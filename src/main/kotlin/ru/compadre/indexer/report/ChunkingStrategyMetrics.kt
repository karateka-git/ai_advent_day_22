package ru.compadre.indexer.report

import ru.compadre.indexer.model.ChunkingStrategy

/**
 * Сводные метрики по одной стратегии chunking.
 */
data class ChunkingStrategyMetrics(
    val strategy: ChunkingStrategy,
    val chunksCount: Int,
    val averageLength: Double,
    val minLength: Int,
    val maxLength: Int,
    val lengthBuckets: List<ChunkLengthBucket>,
)
