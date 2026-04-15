package ru.compadre.indexer.workflow.service

import ru.compadre.indexer.chunking.FixedSizeChunker
import ru.compadre.indexer.chunking.StructuredChunker
import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.embedding.EmbeddingService
import ru.compadre.indexer.loader.DocumentLoader
import ru.compadre.indexer.model.ChunkingStrategy
import ru.compadre.indexer.model.DocumentChunk
import ru.compadre.indexer.model.RawDocument
import ru.compadre.indexer.workflow.command.CompareCommand
import ru.compadre.indexer.workflow.command.HelpCommand
import ru.compadre.indexer.workflow.command.IndexCommand
import ru.compadre.indexer.workflow.command.WorkflowCommand
import ru.compadre.indexer.workflow.result.ChunkEmbeddingPreview
import ru.compadre.indexer.workflow.result.ChunkPreviewResult
import ru.compadre.indexer.workflow.result.CommandResult
import ru.compadre.indexer.workflow.result.HelpResult
import java.nio.file.Path

/**
 * Стартовая реализация обработчика команд для этапов загрузки корпуса, chunking и preview embeddings.
 */
class DefaultWorkflowCommandHandler(
    private val documentLoader: DocumentLoader = DocumentLoader(),
) : WorkflowCommandHandler {
    override suspend fun handle(command: WorkflowCommand, config: AppConfig): CommandResult = when (command) {
        HelpCommand -> HelpResult(
            inputDir = config.app.inputDir,
            outputDir = config.app.outputDir,
            ollamaBaseUrl = config.ollama.baseUrl,
            embeddingModel = config.ollama.embeddingModel,
            fixedSize = config.chunking.fixedSize,
            overlap = config.chunking.overlap,
        )

        is IndexCommand -> buildChunkPreviewResult(
            commandName = "index",
            inputDir = command.inputDir ?: config.app.inputDir,
            config = config,
            strategy = command.strategy,
            allStrategies = command.allStrategies,
            strategyLabel = command.strategy?.id ?: if (command.allStrategies) "all" else "fixed",
        )

        is CompareCommand -> buildChunkPreviewResult(
            commandName = "compare",
            inputDir = command.inputDir ?: config.app.inputDir,
            config = config,
            strategy = null,
            allStrategies = true,
            strategyLabel = "fixed vs structured",
        )
    }

    private suspend fun buildChunkPreviewResult(
        commandName: String,
        inputDir: String,
        config: AppConfig,
        strategy: ChunkingStrategy?,
        allStrategies: Boolean,
        strategyLabel: String,
    ): ChunkPreviewResult {
        val documents = documentLoader.load(Path.of(inputDir))
        val chunks = buildChunks(
            documents = documents,
            config = config,
            strategy = strategy,
            allStrategies = allStrategies,
        )
        val embeddings = buildEmbeddingPreview(chunks, config)

        return ChunkPreviewResult(
            commandName = commandName,
            inputDir = inputDir,
            outputDir = config.app.outputDir,
            strategyLabel = strategyLabel,
            documents = documents,
            chunks = chunks,
            embeddings = embeddings,
        )
    }

    private fun buildChunks(
        documents: List<RawDocument>,
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

    private suspend fun buildEmbeddingPreview(
        chunks: List<DocumentChunk>,
        config: AppConfig,
    ): List<ChunkEmbeddingPreview> {
        if (chunks.isEmpty()) {
            return emptyList()
        }

        val embeddingService = EmbeddingService(config.ollama)
        return try {
            chunks.take(EMBEDDING_PREVIEW_LIMIT).mapNotNull { chunk ->
                val embedding = embeddingService.generate(chunk.text) ?: return@mapNotNull null

                ChunkEmbeddingPreview(
                    chunkId = chunk.metadata.chunkId,
                    model = embedding.model,
                    vectorSize = embedding.vector.size,
                    textPreview = chunk.text,
                )
            }
        } finally {
            embeddingService.close()
        }
    }

    private companion object {
        private const val EMBEDDING_PREVIEW_LIMIT = 3
    }
}
