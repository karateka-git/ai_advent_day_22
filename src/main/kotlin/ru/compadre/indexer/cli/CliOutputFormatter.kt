package ru.compadre.indexer.cli

import ru.compadre.indexer.workflow.result.CommandResult

/**
 * Контракт пользовательского CLI-вывода.
 */
interface CliOutputFormatter {
    /**
     * Форматирует результат выполнения команды в понятный текст для консоли.
     */
    fun format(result: CommandResult): String
}
