package ru.compadre.indexer.workflow.result

import ru.compadre.indexer.model.DocumentChunk
import ru.compadre.indexer.model.RawDocument

/**
 * Базовый тип результатов выполнения CLI-команд.
 */
sealed interface CommandResult

/**
 * Результат вывода справки.
 */
data class HelpResult(
    val inputDir: String,
    val outputDir: String,
    val ollamaBaseUrl: String,
    val embeddingModel: String,
    val fixedSize: Int,
    val overlap: Int,
) : CommandResult

/**
 * Результат предпросмотра загрузки документов для индексации.
 */
data class DocumentLoadResult(
    val commandName: String,
    val inputDir: String,
    val outputDir: String,
    val strategyLabel: String,
    val documents: List<RawDocument>,
) : CommandResult

/**
 * Результат предпросмотра chunking на текущем этапе.
 */
data class ChunkPreviewResult(
    val commandName: String,
    val inputDir: String,
    val outputDir: String,
    val strategyLabel: String,
    val documents: List<RawDocument>,
    val chunks: List<DocumentChunk>,
) : CommandResult
