package ru.compadre.indexer.storage

/**
 * Короткая сводка по локально сохранённому индексу.
 */
data class StoredIndexSummary(
    val documentsCount: Int,
    val chunksCount: Int,
    val embeddingsCount: Int,
    val strategies: List<String>,
)
