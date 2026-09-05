package com.evostackr.smscampaignmanager.util

import java.io.InputStream

data class ParsedContact(
    val name: String?,
    val phoneNumber: String,
    val company: String? = null,
    val date: String? = null,
    val time: String? = null,
    val amount: String? = null,
    val city: String? = null,
    val custom: String? = null
)

data class ImportResult(
    val validContacts: List<ParsedContact>,
    val invalidCount: Int,
    val duplicateCount: Int,
    val totalProcessed: Int
)

object CsvParser {
    fun parseCsv(inputStream: InputStream, removeDuplicates: Boolean = true): ImportResult {
        return try {
            var invalidCount = 0
            var duplicateCount = 0
            var totalProcessed = 0

            val seenNumbers = mutableSetOf<String>()
            val validContacts = mutableListOf<ParsedContact>()

            inputStream.bufferedReader().useLines { lineSequence ->
                for (rawLine in lineSequence) {
                    val line = rawLine.trim()
                    if (line.isEmpty()) continue

                    totalProcessed++
                    val tokens = splitCsvLine(line)
                    if (tokens.isEmpty()) continue

                    // Heuristic for headers: skip line if any token contains letters like "phone" or "mobile"
                    if (totalProcessed == 1 && tokens.any { it.lowercase().contains("phone") || it.lowercase().contains("mobile") || it.lowercase().contains("number") }) {
                        continue
                    }

                    var phoneIndex = -1
                    var sanitizedPhone = ""
                    for (i in tokens.indices) {
                        val potentialPhone = PhoneValidator.sanitizePhoneNumber(tokens[i])
                        if (PhoneValidator.isValidPhoneNumber(potentialPhone)) {
                            phoneIndex = i
                            sanitizedPhone = potentialPhone
                            break
                        }
                    }

                    if (phoneIndex == -1) {
                        invalidCount++
                        continue
                    }

                    val otherTokens = tokens.filterIndexed { index, _ -> index != phoneIndex }

                    val rawName = if (otherTokens.isNotEmpty() && otherTokens[0].isNotBlank()) otherTokens[0].trim() else null
                    val company = if (otherTokens.size > 1 && otherTokens[1].isNotBlank()) otherTokens[1].trim() else null
                    val date = if (otherTokens.size > 2 && otherTokens[2].isNotBlank()) otherTokens[2].trim() else null
                    val time = if (otherTokens.size > 3 && otherTokens[3].isNotBlank()) otherTokens[3].trim() else null
                    val amount = if (otherTokens.size > 4 && otherTokens[4].isNotBlank()) otherTokens[4].trim() else null
                    val city = if (otherTokens.size > 5 && otherTokens[5].isNotBlank()) otherTokens[5].trim() else null
                    val custom = if (otherTokens.size > 6 && otherTokens[6].isNotBlank()) otherTokens[6].trim() else null

                    if (removeDuplicates && seenNumbers.contains(sanitizedPhone)) {
                        duplicateCount++
                        continue
                    }

                    seenNumbers.add(sanitizedPhone)
                    validContacts.add(ParsedContact(
                        name = rawName, 
                        phoneNumber = sanitizedPhone,
                        company = company,
                        date = date,
                        time = time,
                        amount = amount,
                        city = city,
                        custom = custom
                    ))
                }
            }

            ImportResult(
                validContacts = validContacts,
                invalidCount = invalidCount,
                duplicateCount = duplicateCount,
                totalProcessed = totalProcessed
            )
        } catch (e: Exception) {
            ImportResult(
                validContacts = emptyList(),
                invalidCount = 0,
                duplicateCount = 0,
                totalProcessed = 0
            )
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(ch)
                    } else {
                        result.add(sb.toString().trim())
                        sb.setLength(0)
                    }
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    fun parseRawText(rawText: String, removeDuplicates: Boolean = true): ImportResult {
        // Split by lines, commas, or semicolons
        val items = rawText.split(Regex("[\n\r,;]+"))
        var invalidCount = 0
        var duplicateCount = 0
        var totalProcessed = 0

        val seenNumbers = mutableSetOf<String>()
        val validContacts = mutableListOf<ParsedContact>()

        for (item in items) {
            val trimmed = item.trim()
            if (trimmed.isEmpty()) continue

            totalProcessed++
            val sanitized = PhoneValidator.sanitizePhoneNumber(trimmed)
            if (!PhoneValidator.isValidPhoneNumber(sanitized)) {
                invalidCount++
                continue
            }

            if (removeDuplicates && seenNumbers.contains(sanitized)) {
                duplicateCount++
                continue
            }

            seenNumbers.add(sanitized)
            validContacts.add(ParsedContact(name = null, phoneNumber = sanitized))
        }

        return ImportResult(
            validContacts = validContacts,
            invalidCount = invalidCount,
            duplicateCount = duplicateCount,
            totalProcessed = totalProcessed
        )
    }
}

