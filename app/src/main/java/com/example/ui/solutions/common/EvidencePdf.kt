package com.example.ui.solutions.common

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File

/** Writes a plain A4 text report (evidence logs, claims) to the cache dir for sharing. */
object EvidencePdf {
    fun write(context: Context, fileName: String, title: String, lines: List<String>): File? = try {
        val doc = PdfDocument()
        val titlePaint = Paint().apply { textSize = 16f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val body = Paint().apply { textSize = 10.5f; isAntiAlias = true }
        val pageW = 595
        val pageH = 842
        val margin = 40f
        val lineH = 15f
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
        var y = margin + 10f
        page.canvas.drawText(title, margin, y, titlePaint)
        y += 26f
        val maxChars = 95
        val wrapped = lines.flatMap { line ->
            if (line.length <= maxChars) listOf(line) else line.chunked(maxChars)
        }
        for (line in wrapped) {
            if (y > pageH - margin) {
                doc.finishPage(page)
                pageNo++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
                y = margin + 10f
            }
            page.canvas.drawText(line, margin, y, body)
            y += lineH
        }
        doc.finishPage(page)
        val out = File(context.cacheDir, fileName)
        out.outputStream().use { doc.writeTo(it) }
        doc.close()
        out
    } catch (e: Exception) {
        null
    }
}
