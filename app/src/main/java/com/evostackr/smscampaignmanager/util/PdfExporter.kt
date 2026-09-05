package com.evostackr.smscampaignmanager.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportLogsToPdf(outputStream: OutputStream, logs: List<SmsLogEntity>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true

        // Header Title
        canvas.drawText("SMS Campaign Manager - Executive Report", 40f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 40f, 75f, paint)

        val totalSent = logs.count { it.result == SmsLogEntity.RESULT_SUCCESS }
        val totalFailed = logs.count { it.result == SmsLogEntity.RESULT_FAILED }
        val rate = if (logs.isNotEmpty()) (totalSent * 100) / logs.size else 100

        // Summary Card Box
        paint.color = Color.LTGRAY
        canvas.drawRect(40f, 95f, 555f, 160f, paint)

        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Summary Overview:", 55f, 115f, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Total SMS Logs: ${logs.size} | Success: $totalSent | Failed: $totalFailed | Success Rate: $rate%", 55f, 140f, paint)

        // Logs Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Phone Number", 40f, 190f, paint)
        canvas.drawText("Result", 180f, 190f, paint)
        canvas.drawText("Timestamp", 280f, 190f, paint)
        canvas.drawText("Message Snippet", 420f, 190f, paint)

        paint.strokeWidth = 1f
        canvas.drawLine(40f, 195f, 555f, 195f, paint)

        paint.isFakeBoldText = false
        var yPos = 215f

        for (log in logs.take(30)) { // Limit first page to 30 entries
            val phone = if (log.phoneNumber.length > 15) log.phoneNumber.substring(0, 15) else log.phoneNumber
            val result = log.result
            val time = SimpleDateFormat("MM-dd HH:mm", Locale.US).format(Date(log.timestamp))
            val msg = if (log.message.length > 20) log.message.substring(0, 20) + "..." else log.message

            canvas.drawText(phone, 40f, yPos, paint)
            canvas.drawText(result, 180f, yPos, paint)
            canvas.drawText(time, 280f, yPos, paint)
            canvas.drawText(msg, 420f, yPos, paint)

            yPos += 20f
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}

