package ru.compadre.indexer.model

import ru.compadre.indexer.embedding.model.ChunkEmbedding

/**
 * Чанк документа вместе с уже сгенерированным embedding.
 */
data class EmbeddedChunk(
    val chunk: DocumentChunk,
    val embedding: ChunkEmbedding,
)
