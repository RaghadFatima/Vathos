
package com.vathos.app.data.repository

class DocumentChunker(
    private val chunkSize: Int = 300,
    private val overlapSize: Int = 40
) {

    init {
        require(chunkSize > 0) {
            "Chunk size must be greater than zero"
        }

        require(overlapSize >= 0 && overlapSize < chunkSize) {
            "Overlap must be smaller than chunk size"
        }
    }

    fun splitIntoChunks(text: String): List<String> {

        val words = text
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return emptyList()

        val chunks = mutableListOf<String>()
        val step = chunkSize - overlapSize

        var start = 0

        while (start < words.size) {

            val end = minOf(start + chunkSize, words.size)

            val chunk = words
                .subList(start, end)
                .joinToString(" ")

            chunks.add(chunk)

            if (end == words.size) break

            start += step
        }

        return chunks
    }
}
