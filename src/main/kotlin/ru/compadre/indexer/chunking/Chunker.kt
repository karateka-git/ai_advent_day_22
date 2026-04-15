package ru.compadre.indexer.chunking

import ru.compadre.indexer.model.DocumentChunk
import ru.compadre.indexer.model.RawDocument

/**
 * Контракт разбиения документа на чанки.
 */
interface Chunker {
    /**
     * Разбивает документ на чанки в соответствии со своей стратегией.
     */
    fun chunk(document: RawDocument): List<DocumentChunk>
}
