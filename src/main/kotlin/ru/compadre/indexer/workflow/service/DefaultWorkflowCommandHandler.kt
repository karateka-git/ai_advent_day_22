package ru.compadre.indexer.workflow.service

import ru.compadre.indexer.chunking.FixedSizeChunker
import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.loader.DocumentLoader
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.workflow.command.CompareCommand
import ru.compadre.indexer.workflow.command.HelpCommand
import ru.compadre.indexer.workflow.command.IndexCommand
import ru.compadre.indexer.workflow.command.WorkflowCommand
import ru.compadre.indexer.workflow.result.ChunkPreviewResult
import ru.compadre.indexer.workflow.result.CommandResult
import ru.compadre.indexer.workflow.result.DocumentLoadResult
import ru.compadre.indexer.workflow.result.HelpResult
import java.nio.file.Path

/**
 * Стартовая реализация обработчика команд для этапов загрузки корпуса и chunking.
 */
class DefaultWorkflowCommandHandler(
    private val documentLoader: DocumentLoader = DocumentLoader(),
) : WorkflowCommandHandler {
    override fun handle(command: WorkflowCommand, config: AppConfig): CommandResult = when (command) {
        HelpCommand -> HelpResult(
            inputDir = config.app.inputDir,
            outputDir = config.app.outputDir,
            ollamaBaseUrl = config.ollama.baseUrl,
            embeddingModel = config.ollama.embeddingModel,
            fixedSize = config.chunking.fixedSize,
            overlap = config.chunking.overlap,
        )

        is IndexCommand -> {
            val inputDir = command.inputDir ?: config.app.inputDir
            val strategy = command.strategy ?: if (command.allStrategies) null else ChunkingStrategy.FIXED
            val documents = documentLoader.load(Path.of(inputDir))

            if (strategy == null || strategy == ChunkingStrategy.FIXED) {
                val chunker = FixedSizeChunker(
                    chunkSize = config.chunking.fixedSize,
                    overlap = config.chunking.overlap,
                )
                val chunks = documents.flatMap { chunker.chunk(it) }
                ChunkPreviewResult(
                    commandName = "index",
                    inputDir = inputDir,
                    outputDir = config.app.outputDir,
                    strategyLabel = strategy?.id ?: "all (fixed preview)",
                    documents = documents,
                    chunks = chunks,
                )
            } else {
                DocumentLoadResult(
                    commandName = "index",
                    inputDir = inputDir,
                    outputDir = config.app.outputDir,
                    strategyLabel = strategy.id,
                    documents = documents,
                )
            }
        }

        is CompareCommand -> {
            val inputDir = command.inputDir ?: config.app.inputDir
            DocumentLoadResult(
                commandName = "compare",
                inputDir = inputDir,
                outputDir = config.app.outputDir,
                strategyLabel = "fixed vs structured",
                documents = documentLoader.load(Path.of(inputDir)),
            )
        }
    }
}
