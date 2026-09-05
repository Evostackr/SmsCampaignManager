package com.evostackr.smscampaignmanager

import com.evostackr.smscampaignmanager.util.SmsCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class SmsCalculatorTest {

    @Test
    fun calculateSegments_standardGsmText() {
        val shortMessage = "Hello World"
        assertEquals(1, SmsCalculator.calculateSegments(shortMessage))

        val longMessage = "A".repeat(161)
        assertEquals(2, SmsCalculator.calculateSegments(longMessage))
    }

    @Test
    fun calculateSegments_unicodeText() {
        val unicodeMessage = "Hello ★ World"
        assertEquals(1, SmsCalculator.calculateSegments(unicodeMessage))

        val longUnicodeMessage = "★".repeat(71)
        assertEquals(2, SmsCalculator.calculateSegments(longUnicodeMessage))
    }

    @Test
    fun estimateTotalSms_multipliesRecipientsBySegments() {
        val message = "Test message"
        assertEquals(50, SmsCalculator.estimateTotalSms(50, message))
    }
}
