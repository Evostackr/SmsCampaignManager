package com.evostackr.smscampaignmanager.util

import android.content.Context
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.local.entity.ContactEntity
import com.evostackr.smscampaignmanager.data.local.entity.TemplateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

object BackupManager {

    suspend fun exportBackup(context: Context, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val root = JSONObject()

        // 1. Export Templates
        val templates = db.templateDao().getAllTemplatesDirect()
        val templatesArray = JSONArray()
        for (t in templates) {
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("content", t.content)
            obj.put("isFavorite", t.isFavorite)
            templatesArray.put(obj)
        }
        root.put("templates", templatesArray)

        // 2. Export Contacts
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
        root.put("contacts", contactsArray)

        root.put("timestamp", System.currentTimeMillis())
        root.put("version", 1)

        outputStream.bufferedWriter().use { it.write(root.toString(2)) }
    }

    suspend fun restoreBackup(context: Context, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val content = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(content)

            val db = AppDatabase.getInstance(context)

            // Restore Templates
            if (root.has("templates")) {
                val array = root.getJSONArray("templates")
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
            if (root.has("contacts")) {
                val array = root.getJSONArray("contacts")
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
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

