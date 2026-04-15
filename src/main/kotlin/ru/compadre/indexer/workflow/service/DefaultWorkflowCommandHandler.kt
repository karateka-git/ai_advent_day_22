package ru.compadre.indexer.workflow.service

import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.loader.DocumentLoader
import ru.compadre.indexer.workflow.command.CompareCommand
import ru.compadre.indexer.workflow.command.HelpCommand
import ru.compadre.indexer.workflow.command.IndexCommand
import ru.compadre.indexer.workflow.command.WorkflowCommand
import ru.compadre.indexer.workflow.result.CommandResult
import ru.compadre.indexer.workflow.result.DocumentLoadResult
import ru.compadre.indexer.workflow.result.HelpResult
import java.nio.file.Path

/**
 * Стартовая реализация обработчика команд для этапов загрузки корпуса.
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
            val strategyLabel = command.strategy ?: if (command.allStrategies) "all" else "<не указана>"
            DocumentLoadResult(
                commandName = "index",
                inputDir = inputDir,
                outputDir = config.app.outputDir,
                strategyLabel = strategyLabel,
                documents = documentLoader.load(Path.of(inputDir)),
            )
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
