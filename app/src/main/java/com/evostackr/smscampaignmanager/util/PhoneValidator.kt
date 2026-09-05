package com.evostackr.smscampaignmanager.util

object PhoneValidator {
    /**
     * Sanitizes phone number by removing spaces, dashes, brackets, non-numeric characters (except leading +).
     */
    fun sanitizePhoneNumber(phone: String): String {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) return ""
        val hasLeadingPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        return if (hasLeadingPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Validates if the phone number has a valid format (7-15 digits).
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val sanitized = sanitizePhoneNumber(phone)
        val digits = if (sanitized.startsWith("+")) sanitized.substring(1) else sanitized
        return digits.length in 7..15 && digits.all { it.isDigit() }
    }
}

