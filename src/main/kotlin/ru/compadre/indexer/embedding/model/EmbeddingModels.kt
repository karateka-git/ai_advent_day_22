package ru.compadre.indexer.embedding.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * HTTP-модели для работы с Ollama embeddings API.
 */
@Serializable
data class OllamaEmbedRequest(
    @SerialName("model")
    val model: String,
    @SerialName("input")
    val input: String,
)

@Serializable
data class OllamaEmbedResponse(
    @SerialName("model")
    val model: String,
    @SerialName("embeddings")
    val embeddings: List<List<Float>>,
)

/**
 * Результат генерации embedding для одного текстового фрагмента.
 */
data class ChunkEmbedding(
    val model: String,
    val vector: List<Float>,
)
