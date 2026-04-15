package ru.compadre.indexer.model

/**
 * Поддерживаемые стратегии chunking в проекте.
 */
enum class ChunkingStrategy(val id: String) {
    FIXED("fixed"),
    STRUCTURED("structured");

    companion object {
        fun fromCli(value: String): ChunkingStrategy? =
            entries.firstOrNull { it.id == value.trim().lowercase() }
    }
}
