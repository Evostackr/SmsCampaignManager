package com.evostackr.smscampaignmanager.util

import org.junit.Assert.*
import org.junit.Test

class MmsUnitTests {

    @Test
    fun testPhoneValidator() {
        assertTrue(PhoneValidator.isValidPhoneNumber("+12345678901"))
        assertTrue(PhoneValidator.isValidPhoneNumber("9876543210"))
        assertFalse(PhoneValidator.isValidPhoneNumber("abc"))
        assertFalse(PhoneValidator.isValidPhoneNumber("123"))
    }

    @Test
    fun testSmsCalculator() {
        val gsmText = "Hello world"
        val segments = SmsCalculator.calculateSegments(gsmText)
        assertEquals(1, segments)

        val totalSms = SmsCalculator.estimateTotalSms(10, gsmText)
        assertEquals(10, totalSms)
    }

    @Test
    fun testPermissionManagerExplanations() {
        val sendSmsExp = PermissionManager.getPermissionExplanation(android.Manifest.permission.SEND_SMS)
        assertTrue(sendSmsExp.contains("SMS"))

        val cameraExp = PermissionManager.getPermissionExplanation(android.Manifest.permission.CAMERA)
        assertTrue(cameraExp.contains("Camera"))

        val mediaExp = PermissionManager.getPermissionExplanation(android.Manifest.permission.READ_MEDIA_IMAGES)
        assertTrue(mediaExp.contains("Media"))
    }
}
