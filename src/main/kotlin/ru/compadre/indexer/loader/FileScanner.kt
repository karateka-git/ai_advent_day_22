package ru.compadre.indexer.loader

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * Рекурсивно находит поддерживаемые документы в указанной директории.
 */
class FileScanner {
    fun scan(inputDir: Path): List<Path> =
        Files.walk(inputDir).use { stream ->
            stream
                .filter { it.isRegularFile() }
                .filter { SourceTypeDetector.detect(it) != null }
                .sorted()
                .toList()
        }
}
