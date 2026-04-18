package ru.compadre.indexer.search

import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.embedding.EmbeddingService
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.search.model.SearchMatch
import ru.compadre.indexer.storage.IndexStore
import ru.compadre.indexer.storage.SqliteIndexStore
import java.nio.file.Path

/**
 * Прозрачная учебная реализация retrieval через полный перебор всех embeddings.
 */
class BruteForceSearchEngine(
    private val indexStore: IndexStore = SqliteIndexStore(),
) : SearchEngine {
    override suspend fun search(
        query: String,
        databasePath: Path,
        strategy: ChunkingStrategy?,
        topK: Int,
        config: AppConfig,
    ): List<SearchMatch> {
        require(topK > 0) { "Параметр topK должен быть больше 0." }

        val embeddedChunks = indexStore.readEmbeddedChunks(
            databasePath = databasePath,
            strategy = strategy,
        )
        if (embeddedChunks.isEmpty()) {
            return emptyList()
        }

        val embeddingService = EmbeddingService(config.ollama)
        return try {
            val queryEmbedding = embeddingService.generate(query)
                ?: return emptyList()

            embeddedChunks
                .asSequence()
                .map { embeddedChunk ->
                    SearchMatch(
                        embeddedChunk = embeddedChunk,
                        score = CosineSimilarity.calculate(
                            left = queryEmbedding.vector,
                            right = embeddedChunk.embedding.vector,
                        ),
                    )
                }
                .sortedByDescending { it.score }
                .take(topK)
                .toList()
        } finally {
            embeddingService.close()
        }
    }
}
