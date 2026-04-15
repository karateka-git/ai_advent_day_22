package ru.compadre.indexer.workflow.command

import ru.compadre.indexer.model.ChunkingStrategy

/**
 * Команда запуска индексации.
 */
data class IndexCommand(
    val inputDir: String?,
    val strategy: ChunkingStrategy?,
    val allStrategies: Boolean,
) : WorkflowCommand
