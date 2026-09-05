package com.evostackr.smscampaignmanager

import com.evostackr.smscampaignmanager.util.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvParserTest {

    @Test
    fun parseCsv_extractsValidContactsAndFiltersDuplicates() {
        val csvData = """
            Phone Number, Name
            +1234567890, John Doe
            +1234567890, John Duplicate
            invalid_phone, Alice
            +9876543210, Bob Smith
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvData.toByteArray())
        val result = CsvParser.parseCsv(inputStream, removeDuplicates = true)

        assertEquals(2, result.validContacts.size)
        assertEquals(1, result.duplicateCount)
        assertEquals(1, result.invalidCount)
        assertEquals("+1234567890", result.validContacts[0].phoneNumber)
        assertEquals("John Doe", result.validContacts[0].name)
    }

    @Test
    fun parseRawText_handlesMultipleSeparators() {
        val rawText = "+1234567890, +9876543210; +1234567890\n+1122334455"
        val result = CsvParser.parseRawText(rawText, removeDuplicates = true)

        assertEquals(3, result.validContacts.size)
        assertEquals(1, result.duplicateCount)
    }
}
