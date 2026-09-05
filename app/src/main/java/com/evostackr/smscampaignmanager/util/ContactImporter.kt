package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.net.Uri
import com.evostackr.smscampaignmanager.data.local.entity.ImportedContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object ContactImporter {

    suspend fun importFromUri(context: Context, uri: Uri, isExcel: Boolean): List<ImportedContactEntity> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ImportedContactEntity>()
        val sourceLabel = if (isExcel) "EXCEL" else "CSV"

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    var line: String?
                    var isHeader = true
                    var nameIndex = 0
                    var phoneIndex = 1
                    var tagIndex = 2
                    var companyIndex = 3
                    var notesIndex = 4

                    while (reader.readLine().also { line = it } != null) {
                        val row = line ?: continue
                        if (row.isBlank()) continue

                        val tokens = row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                            .map { it.replace("\"", "").trim() }

                        if (isHeader) {
                            // Check header column order
                            tokens.forEachIndexed { index, col ->
                                val lower = col.lowercase()
                                when {
                                    lower.contains("name") -> nameIndex = index
                                    lower.contains("phone") || lower.contains("mobile") || lower.contains("num") -> phoneIndex = index
                                    lower.contains("tag") || lower.contains("group") -> tagIndex = index
                                    lower.contains("company") || lower.contains("org") -> companyIndex = index
                                    lower.contains("note") || lower.contains("memo") -> notesIndex = index
                                }
                            }
                            isHeader = false
                            continue
                        }

                        if (tokens.size > nameIndex && tokens.size > phoneIndex) {
                            val name = tokens.getOrNull(nameIndex)?.takeIf { it.isNotBlank() } ?: continue
                            val phoneRaw = tokens.getOrNull(phoneIndex)?.takeIf { it.isNotBlank() } ?: continue
                            val phoneClean = phoneRaw.replace("[^0-9+]".toRegex(), "")

                            if (phoneClean.isNotBlank()) {
                                contacts.add(
                                    ImportedContactEntity(
                                        name = name,
                                        phoneNumber = phoneClean,
                                        tag = tokens.getOrNull(tagIndex)?.takeIf { it.isNotBlank() },
                                        company = tokens.getOrNull(companyIndex)?.takeIf { it.isNotBlank() },
                                        notes = tokens.getOrNull(notesIndex)?.takeIf { it.isNotBlank() },
                                        source = sourceLabel
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contacts
    }
}

