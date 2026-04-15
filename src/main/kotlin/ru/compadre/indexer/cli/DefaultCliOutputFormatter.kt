package ru.compadre.indexer.cli

import ru.compadre.indexer.workflow.result.CommandResult
import ru.compadre.indexer.workflow.result.DocumentLoadResult
import ru.compadre.indexer.workflow.result.HelpResult

/**
 * Форматтер CLI-вывода для стартового этапа проекта.
 */
class DefaultCliOutputFormatter : CliOutputFormatter {
    override fun format(result: CommandResult): String = when (result) {
        is HelpResult -> helpText(result)
        is DocumentLoadResult -> documentLoadText(result)
    }

    private fun helpText(result: HelpResult): String = buildList {
        add("Local Document Indexer")
        add("")
        add("Доступные команды:")
        add("  index --input <dir> --strategy <fixed|structured>")
        add("  index --input <dir> --all-strategies")
        add("  compare --input <dir>")
        add("  help")
        add("")
        add("Текущий конфиг:")
        add("  inputDir = ${result.inputDir}")
        add("  outputDir = ${result.outputDir}")
        add("  ollama.baseUrl = ${result.ollamaBaseUrl}")
        add("  ollama.embeddingModel = ${result.embeddingModel}")
        add("  chunking.fixedSize = ${result.fixedSize}")
        add("  chunking.overlap = ${result.overlap}")
        add("")
        add("Текущий статус: каркас CLI и конфиг подключены, этап загрузки документов активен.")
    }.joinToString(separator = System.lineSeparator())

    private fun documentLoadText(result: DocumentLoadResult): String = buildList {
        add("Команда `${result.commandName}` выполнила загрузку документов.")
        add("")
        add("Параметры запуска:")
        add("  inputDir = ${result.inputDir}")
        add("  strategy = ${result.strategyLabel}")
        add("  outputDir = ${result.outputDir}")
        add("")
        add("Найдено документов: ${result.documents.size}")

        if (result.documents.isEmpty()) {
            add("Поддерживаемые документы не найдены.")
        } else {
            add("Первые документы:")
            result.documents.take(10).forEach { document ->
                add("  - [${document.sourceType}] ${document.fileName} -> ${document.filePath}")
                add("    textLength = ${document.text?.length ?: 0}")
                add("    preview = ${previewText(document.text)}")
            }
        }
    }.joinToString(separator = System.lineSeparator())

    private fun previewText(text: String?): String {
        if (text.isNullOrBlank()) {
            return "<пусто>"
        }

        val singleLine = text.replace(Regex("\\s+"), " ").trim()
        return if (singleLine.length <= 80) {
            singleLine
        } else {
            singleLine.take(77) + "..."
        }
    }
}
