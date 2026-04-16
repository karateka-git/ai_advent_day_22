package ru.compadre.indexer.report

/**
 * Бакет распределения длин чанков для отчёта.
 */
data class ChunkLengthBucket(
    val rangeLabel: String,
    val count: Int,
)
