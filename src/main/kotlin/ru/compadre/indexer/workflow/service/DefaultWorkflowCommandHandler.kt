package ru.compadre.indexer.workflow.service

import ru.compadre.indexer.chunking.FixedSizeChunker
import ru.compadre.indexer.chunking.StructuredChunker
import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.loader.DocumentLoader
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.model.DocumentChunk
import ru.compadre.indexer.workflow.command.CompareCommand
import ru.compadre.indexer.workflow.command.HelpCommand
import ru.compadre.indexer.workflow.command.IndexCommand
import ru.compadre.indexer.workflow.command.WorkflowCommand
import ru.compadre.indexer.workflow.result.ChunkPreviewResult
import ru.compadre.indexer.workflow.result.CommandResult
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
            val documents = documentLoader.load(Path.of(inputDir))
            val chunks = buildChunks(
                documents = documents,
                config = config,
                strategy = command.strategy,
                allStrategies = command.allStrategies,
            )

            ChunkPreviewResult(
                commandName = "index",
                inputDir = inputDir,
                outputDir = config.app.outputDir,
                strategyLabel = command.strategy?.id ?: if (command.allStrategies) "all" else "fixed",
                documents = documents,
                chunks = chunks,
            )
        }

        is CompareCommand -> {
            val inputDir = command.inputDir ?: config.app.inputDir
            val documents = documentLoader.load(Path.of(inputDir))
            val chunks = buildChunks(
                documents = documents,
                config = config,
                strategy = null,
                allStrategies = true,
            )

            ChunkPreviewResult(
                commandName = "compare",
                inputDir = inputDir,
                outputDir = config.app.outputDir,
                strategyLabel = "fixed vs structured",
                documents = documents,
                chunks = chunks,
            )
        }
    }

    private fun buildChunks(
        documents: List<ru.compadre.indexer.model.RawDocument>,
        config: AppConfig,
        strategy: ChunkingStrategy?,
        allStrategies: Boolean,
    ): List<DocumentChunk> {
        val fixedChunker = FixedSizeChunker(
            chunkSize = config.chunking.fixedSize,
            overlap = config.chunking.overlap,
        )
        val structuredChunker = StructuredChunker(fallbackChunker = fixedChunker)

        return when {
            allStrategies -> documents.flatMap { document ->
                fixedChunker.chunk(document) + structuredChunker.chunk(document)
            }

            strategy == ChunkingStrategy.STRUCTURED -> documents.flatMap(structuredChunker::chunk)
            else -> documents.flatMap(fixedChunker::chunk)
        }
    }
}
