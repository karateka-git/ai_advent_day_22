package ru.compadre.indexer.embedding

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.compadre.indexer.embedding.model.OllamaEmbedRequest
import ru.compadre.indexer.embedding.model.OllamaEmbedResponse

/**
 * Минимальный HTTP-клиент для запроса embeddings из локального Ollama.
 */
class OllamaEmbeddingClient {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }

    suspend fun embed(
        baseUrl: String,
        model: String,
        input: String,
    ): OllamaEmbedResponse =
        httpClient.post("${baseUrl.trimEnd('/')}/api/embed") {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaEmbedRequest(
                    model = model,
                    input = input,
                ),
            )
        }.body()

    suspend fun close() {
        httpClient.close()
    }
}
