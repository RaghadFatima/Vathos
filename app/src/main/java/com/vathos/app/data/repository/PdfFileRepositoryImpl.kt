
package com.vathos.app.data.repository

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.vathos.app.domain.model.ImportedPdf
import com.vathos.app.domain.repository.PdfFileRepository
import java.io.File
import java.io.FileOutputStream

class PdfFileRepositoryImpl(
    context: Context
) : PdfFileRepository {

    private val appContext = context.applicationContext

    private val documentsDirectory =
        File(appContext.filesDir, "documents").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException(
                    "Could not create documents directory"
                )
            }
        }

    override suspend fun importPdf(
        uriString: String
    ): ImportedPdf {

        require(uriString.isNotBlank()) {
            "PDF URI cannot be empty"
        }

        val uri = Uri.parse(uriString)

        val originalName = getFileName(uri)
        require(originalName.endsWith(".pdf", ignoreCase = true)) {
            "Please select a PDF file"
        }

        val safeName = originalName
            .replace(Regex("[^a-zA-Z0-9._ -]"), "_")

        val destination = File(
            documentsDirectory,
            "${System.currentTimeMillis()}_$safeName"
        )

        try {
            val inputStream = appContext.contentResolver
                .openInputStream(uri)
                ?: throw IllegalStateException(
                    "Unable to open selected PDF"
                )

            inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }

            require(destination.length() > 0L) {
                "The selected PDF is empty"
            }

            val pageCount = countPages(destination)

            return ImportedPdf(
                name = originalName,
                size = destination.length(),
                pages = pageCount,
                filePath = destination.absolutePath
            )

        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    override suspend fun deletePdf(filePath: String) {
        val file = getManagedFile(filePath)

        if (file.exists() && !file.delete()) {
            throw IllegalStateException(
                "Could not delete PDF file"
            )
        }
    }

    override suspend fun deleteAllPdfs() {
        documentsDirectory.listFiles()?.forEach { file ->
            if (file.isFile && !file.delete()) {
                throw IllegalStateException(
                    "Could not delete ${file.name}"
                )
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name: String? = null

        appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

                if (index >= 0) {
                    name = cursor.getString(index)
                }
            }
        }

        return name
            ?.takeIf { it.isNotBlank() }
            ?: "document.pdf"
    }

    private fun countPages(file: File): Int {
        val descriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )

        descriptor.use {
            PdfRenderer(it).use { renderer ->
                return renderer.pageCount
            }
        }
    }

    private fun getManagedFile(filePath: String): File {
        val file = File(filePath).canonicalFile
        val directory = documentsDirectory.canonicalFile

        require(
            file.parentFile == directory &&
                    file.extension.equals("pdf", ignoreCase = true)
        ) {
            "Invalid document storage path"
        }

        return file
    }
}
