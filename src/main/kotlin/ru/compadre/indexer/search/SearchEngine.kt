package ru.compadre.indexer.search

import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.search.model.SearchMatch
import java.nio.file.Path

/**
 * Контракт поискового движка по локальному индексу.
 */
interface SearchEngine {
    /**
     * Выполняет semantic search по сохранённому индексу.
     */
    suspend fun search(
        query: String,
        databasePath: Path,
        strategy: ChunkingStrategy? = null,
        topK: Int,
        config: AppConfig,
    ): List<SearchMatch>
}
