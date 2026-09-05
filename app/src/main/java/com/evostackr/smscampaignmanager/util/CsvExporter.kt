package com.evostackr.smscampaignmanager.util

import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun exportRecipientsToCsv(outputStream: OutputStream, recipients: List<RecipientEntity>) {
        outputStream.bufferedWriter().use { writer ->
            writer.write("ID,Name,Phone Number,Status,Retry Count,Sent Time,Error Reason\n")
            for (rec in recipients) {
                val nameEscaped = escapeCsv(rec.name ?: "")
                val phoneEscaped = escapeCsv(rec.phoneNumber)
                val statusEscaped = escapeCsv(rec.status)
                val sentTimeStr = rec.sentTime?.let { dateFormat.format(Date(it)) } ?: ""
                val errorEscaped = escapeCsv(rec.errorReason ?: "")

                writer.write("${rec.id},$nameEscaped,$phoneEscaped,$statusEscaped,${rec.retryCount},$sentTimeStr,$errorEscaped\n")
            }
        }
    }

    fun exportLogsToCsv(outputStream: OutputStream, logs: List<SmsLogEntity>) {
        outputStream.bufferedWriter().use { writer ->
            writer.write("ID,Phone Number,Message,Timestamp,Result,Campaign ID,Error Message\n")
            for (log in logs) {
                val phoneEscaped = escapeCsv(log.phoneNumber)
                val messageEscaped = escapeCsv(log.message)
                val timestampStr = dateFormat.format(Date(log.timestamp))
                val resultEscaped = escapeCsv(log.result)
                val campaignIdStr = log.campaignId?.toString() ?: ""
                val errorEscaped = escapeCsv(log.errorMessage ?: "")

                writer.write("${log.id},$phoneEscaped,$messageEscaped,$timestampStr,$resultEscaped,$campaignIdStr,$errorEscaped\n")
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}

