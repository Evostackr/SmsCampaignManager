package com.evostackr.smscampaignmanager

import com.evostackr.smscampaignmanager.util.PhoneValidator
import org.junit.Assert.*
import org.junit.Test

class PhoneValidatorTest {

    @Test
    fun sanitizePhoneNumber_stripsFormattingCharacters() {
        assertEquals("+1234567890", PhoneValidator.sanitizePhoneNumber("+1 (234) 567-890"))
        assertEquals("9876543210", PhoneValidator.sanitizePhoneNumber("987-654-3210"))
    }

    @Test
    fun isValidPhoneNumber_validatesLengthsCorrectly() {
        assertTrue(PhoneValidator.isValidPhoneNumber("+1234567890"))
        assertTrue(PhoneValidator.isValidPhoneNumber("1234567890"))
        assertFalse(PhoneValidator.isValidPhoneNumber("123")) // Too short
        assertFalse(PhoneValidator.isValidPhoneNumber("abcdefghijk")) // Non-digits
    }
}
