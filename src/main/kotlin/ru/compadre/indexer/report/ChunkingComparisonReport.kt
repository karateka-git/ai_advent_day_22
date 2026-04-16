package ru.compadre.indexer.report

/**
 * Итоговый отчёт сравнения fixed и structured chunking.
 */
data class ChunkingComparisonReport(
    val inputDir: String,
    val documentsCount: Int,
    val fixedMetrics: ChunkingStrategyMetrics,
    val structuredMetrics: ChunkingStrategyMetrics,
)
