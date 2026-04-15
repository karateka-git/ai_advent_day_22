package ru.compadre.indexer.chunking

import ru.compadre.indexer.model.ChunkMetadata
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.model.DocumentChunk
import ru.compadre.indexer.model.RawDocument
import ru.compadre.indexer.model.SourceType

/**
 * Пытается разбивать документ по естественной структуре и откатывается к fixed-size, если структура не найдена.
 */
class StructuredChunker(
    private val fallbackChunker: FixedSizeChunker,
) : Chunker {
    override fun chunk(document: RawDocument): List<DocumentChunk> {
        val chunks = when (document.sourceType) {
            SourceType.README,
            SourceType.MARKDOWN -> chunkMarkdown(document)

            SourceType.CODE -> chunkCode(document)
            SourceType.TEXT,
            SourceType.PDF -> chunkByParagraphs(document)
        }

        return if (chunks.isEmpty()) {
            fallbackChunker.chunk(document).map { chunk ->
                chunk.copy(
                    metadata = chunk.metadata.copy(
                        chunkId = chunk.metadata.chunkId.replace("#fixed-", "#structured-fallback-"),
                    ),
                    strategy = ChunkingStrategy.STRUCTURED,
                )
            }
        } else {
            chunks
        }
    }

    private fun chunkMarkdown(document: RawDocument): List<DocumentChunk> {
        val headingRegex = Regex("(?m)^(#{1,3})\\s+(.+)$")
        val matches = headingRegex.findAll(document.text).toList()
        if (matches.isEmpty()) {
            return emptyList()
        }

        return matches.mapIndexedNotNull { index, match ->
            val sectionTitle = match.groupValues[2].trim()
            val startOffset = match.range.first
            val endOffset = if (index + 1 < matches.size) {
                matches[index + 1].range.first
            } else {
                document.text.length
            }

            buildChunk(
                document = document,
                chunkIndex = index,
                section = sectionTitle,
                startOffset = startOffset,
                endOffset = endOffset,
            )
        }
    }

    private fun chunkCode(document: RawDocument): List<DocumentChunk> {
        val declarationRegex = Regex("(?m)^(class|interface|object|enum|fun)\\s+([A-Za-z_][A-Za-z0-9_]*)")
        val matches = declarationRegex.findAll(document.text).toList()
        if (matches.isEmpty()) {
            return emptyList()
        }

        return matches.mapIndexedNotNull { index, match ->
            val sectionTitle = match.groupValues[2].trim()
            val startOffset = match.range.first
            val endOffset = if (index + 1 < matches.size) {
                matches[index + 1].range.first
            } else {
                document.text.length
            }

            buildChunk(
                document = document,
                chunkIndex = index,
                section = sectionTitle,
                startOffset = startOffset,
                endOffset = endOffset,
            )
        }
    }

    private fun chunkByParagraphs(document: RawDocument): List<DocumentChunk> {
        val paragraphs = Regex("\\n\\s*\\n").findAll(document.text).toList()
        if (paragraphs.isEmpty()) {
            return emptyList()
        }

        var currentStart = 0
        val chunks = mutableListOf<DocumentChunk>()

        paragraphs.forEachIndexed { index, separator ->
            val endOffset = separator.range.first
            buildChunk(
                document = document,
                chunkIndex = index,
                section = document.title,
                startOffset = currentStart,
                endOffset = endOffset,
            )?.let(chunks::add)
            currentStart = separator.range.last + 1
        }

        buildChunk(
            document = document,
            chunkIndex = chunks.size,
            section = document.title,
            startOffset = currentStart,
            endOffset = document.text.length,
        )?.let(chunks::add)

        return chunks
    }

    private fun buildChunk(
        document: RawDocument,
        chunkIndex: Int,
        section: String,
        startOffset: Int,
        endOffset: Int,
    ): DocumentChunk? {
        val chunkText = document.text.substring(startOffset, endOffset).trim()
        if (chunkText.isEmpty()) {
            return null
        }

        return DocumentChunk(
            metadata = ChunkMetadata(
                chunkId = "${document.documentId}#structured-$chunkIndex",
                documentId = document.documentId,
                sourceType = document.sourceType,
                filePath = document.filePath,
                title = document.title,
                section = section,
                startOffset = startOffset,
                endOffset = endOffset,
            ),
            strategy = ChunkingStrategy.STRUCTURED,
            text = chunkText,
        )
    }
}
