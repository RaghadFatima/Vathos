
package com.vathos.app.data.repository

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

class PdfTextExtractor(context: Context) {

    init {
        PDFBoxResourceLoader.init(context.applicationContext)
    }

    fun extractText(filePath: String): String {
        val file = File(filePath)

        require(file.exists() && file.isFile) {
            "PDF file does not exist"
        }

        require(file.extension.equals("pdf", ignoreCase = true)) {
            "Invalid PDF file"
        }

        PDDocument.load(file).use { document ->

            require(!document.isEncrypted) {
                "Password-protected PDFs are not supported"
            }

            val stripper = PDFTextStripper()

            return stripper.getText(document)
                .trim()
        }
    }
}
