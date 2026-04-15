package ru.compadre.indexer.embedding

import ru.compadre.indexer.config.OllamaSection
import ru.compadre.indexer.embedding.model.ChunkEmbedding

/**
 * Сервис генерации embeddings через локальный Ollama.
 */
class EmbeddingService(
    private val ollamaConfig: OllamaSection,
    private val embeddingClient: OllamaEmbeddingClient = OllamaEmbeddingClient(),
) {
    suspend fun generate(text: String): ChunkEmbedding {
        val response = embeddingClient.embed(
            baseUrl = ollamaConfig.baseUrl,
            model = ollamaConfig.embeddingModel,
            input = text,
        )

        val vector = response.embeddings.firstOrNull()
            ?: error("Ollama вернул пустой список embeddings.")

        return ChunkEmbedding(
            model = response.model,
            vector = vector,
        )
    }

    suspend fun close() {
        embeddingClient.close()
    }
}
