package com.evostackr.smscampaignmanager.util

import android.content.Context
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CorruptedBackupException(message: String) : Exception(message)

object EncryptedBackupEngine {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256
    private const val MAGIC_HEADER = "SMS_CAMPAIGN_ENCRYPTED_BACKUP_V2"

    suspend fun createEncryptedBackup(context: Context, outputStream: OutputStream): Long = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val rawJson = JSONObject()

        // 1. Serialize Campaigns
        val campaigns = db.campaignDao().getAllCampaignsDirect()
        val campaignsArray = JSONArray()
        for (c in campaigns) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("messageTemplate", c.messageTemplate)
            obj.put("delaySeconds", c.delaySeconds)
            obj.put("status", c.status)
            obj.put("totalRecipients", c.totalRecipients)
            obj.put("sentCount", c.sentCount)
            obj.put("failedCount", c.failedCount)
            obj.put("createdAt", c.createdAt)
            campaignsArray.put(obj)
        }
        rawJson.put("campaigns", campaignsArray)

        // 2. Serialize Contacts & Groups
        val contacts = db.contactDao().getAllContactsDirect()
        val contactsArray = JSONArray()
        for (c in contacts) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("phoneNumber", c.phoneNumber)
            obj.put("email", c.email ?: "")
            obj.put("groupTag", c.groupTag ?: "")
            obj.put("isFavorite", c.isFavorite)
            contactsArray.put(obj)
        }
        rawJson.put("contacts", contactsArray)

        // 3. Serialize Templates
        val templates = db.templateDao().getAllTemplatesDirect()
        val templatesArray = JSONArray()
        for (t in templates) {
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("content", t.content)
            obj.put("isFavorite", t.isFavorite)
            templatesArray.put(obj)
        }
        rawJson.put("templates", templatesArray)

        rawJson.put("backupTimestamp", System.currentTimeMillis())
        rawJson.put("appVersion", "2.0")

        val jsonString = rawJson.toString()
        val sha256Checksum = computeSha256(jsonString)

        val payload = JSONObject().apply {
            put("magic", MAGIC_HEADER)
            put("checksum", sha256Checksum)
            put("data", jsonString)
        }.toString()

        val encryptedBytes = encryptAES256(payload.toByteArray(Charsets.UTF_8), SecurityUtil.getOrCreatePassphrase(context))
        outputStream.use { it.write(encryptedBytes) }
        return@withContext encryptedBytes.size.toLong()
    }

    suspend fun restoreEncryptedBackup(context: Context, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        val encryptedBytes = inputStream.use { it.readBytes() }
        val decryptedBytes = try {
            decryptAES256(encryptedBytes, SecurityUtil.getOrCreatePassphrase(context))
        } catch (e: Exception) {
            throw CorruptedBackupException("Decryption failed. Invalid passphrase or corrupted file.")
        }

        val payloadString = String(decryptedBytes, Charsets.UTF_8)
        val payloadObj = try {
            JSONObject(payloadString)
        } catch (e: Exception) {
            throw CorruptedBackupException("Backup file format is invalid JSON.")
        }

        if (payloadObj.optString("magic") != MAGIC_HEADER) {
            throw CorruptedBackupException("Invalid backup header signature.")
        }

        val expectedChecksum = payloadObj.getString("checksum")
        val jsonString = payloadObj.getString("data")
        val actualChecksum = computeSha256(jsonString)

        if (expectedChecksum != actualChecksum) {
            throw CorruptedBackupException("Integrity check failed! SHA-256 checksum mismatch.")
        }

        val db = AppDatabase.getInstance(context)
        val rawJson = JSONObject(jsonString)

        // Restore Templates
        if (rawJson.has("templates")) {
            val array = rawJson.getJSONArray("templates")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                db.templateDao().insertTemplate(
                    TemplateEntity(
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }
        }

        // Restore Contacts
        if (rawJson.has("contacts")) {
            val array = rawJson.getJSONArray("contacts")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                db.contactDao().insertContact(
                    ContactEntity(
                        name = obj.getString("name"),
                        phoneNumber = obj.getString("phoneNumber"),
                        email = obj.optString("email", null),
                        groupTag = obj.optString("groupTag", null),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }
        }
        return@withContext true
    }

    private fun encryptAES256(plainText: ByteArray, passphrase: ByteArray): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }

        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val encrypted = cipher.doFinal(plainText)

        // Structure: Salt (16b) + IV (16b) + EncryptedBytes
        return salt + iv + encrypted
    }

    private fun decryptAES256(cipherText: ByteArray, passphrase: ByteArray): ByteArray {
        if (cipherText.size < 32) throw IllegalArgumentException("Ciphertext too short.")

        val salt = cipherText.copyOfRange(0, 16)
        val iv = cipherText.copyOfRange(16, 32)
        val encryptedData = cipherText.copyOfRange(32, cipherText.size)

        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        return cipher.doFinal(encryptedData)
    }

    private fun deriveKey(passphrase: ByteArray, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(String(passphrase).toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}

