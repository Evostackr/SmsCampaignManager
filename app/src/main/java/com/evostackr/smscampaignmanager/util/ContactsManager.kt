package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactsManager {

    suspend fun getDeviceContacts(context: Context): List<ParsedContact> = withContext(Dispatchers.IO) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return@withContext emptyList()

        val contactsList = mutableListOf<ParsedContact>()
        val seenNumbers = mutableSetOf<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    val rawPhone = if (numberIndex >= 0) cursor.getString(numberIndex) else null ?: continue
                    val sanitizedPhone = PhoneValidator.sanitizePhoneNumber(rawPhone)

                    if (PhoneValidator.isValidPhoneNumber(sanitizedPhone) && !seenNumbers.contains(sanitizedPhone)) {
                        seenNumbers.add(sanitizedPhone)
                        contactsList.add(ParsedContact(name = name, phoneNumber = sanitizedPhone))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext contactsList
    }
}

