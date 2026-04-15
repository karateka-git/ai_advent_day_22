package ru.compadre.indexer.workflow.service

import ru.compadre.indexer.config.AppConfig
import ru.compadre.indexer.workflow.command.WorkflowCommand
import ru.compadre.indexer.workflow.result.CommandResult

/**
 * Выполняет внутренние команды CLI и возвращает результат для вывода.
 */
interface WorkflowCommandHandler {
    fun handle(command: WorkflowCommand, config: AppConfig): CommandResult
}

