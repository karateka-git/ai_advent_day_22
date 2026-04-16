package ru.compadre.indexer.report

import ru.compadre.indexer.model.ChunkingStrategy

/**
 * Сводные метрики по одной стратегии chunking.
 *
 * @param strategy стратегия chunking, к которой относятся рассчитанные метрики
 * @param chunksCount общее количество чанков, сформированных этой стратегией
 * @param averageLength средняя длина чанка в символах
 * @param minLength минимальная длина чанка в символах
 * @param maxLength максимальная длина чанка в символах
 * @param lengthBuckets распределение длин чанков по диапазонам для быстрого визуального сравнения
 */
data class ChunkingStrategyMetrics(
    val strategy: ChunkingStrategy,
    val chunksCount: Int,
    val averageLength: Double,
    val minLength: Int,
    val maxLength: Int,
    val lengthBuckets: List<ChunkLengthBucket>,
)
