package ru.compadre.indexer.loader

import ru.compadre.indexer.extractor.PdfTextExtractor
import ru.compadre.indexer.extractor.PlainTextExtractor
import ru.compadre.indexer.extractor.TextExtractorRegistry
import ru.compadre.indexer.extractor.TextNormalizer
import ru.compadre.indexer.model.RawDocument
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * Преобразует поддерживаемые файлы в унифицированную модель `RawDocument`.
 */
class DocumentLoader(
    private val fileScanner: FileScanner = FileScanner(),
    private val extractorRegistry: TextExtractorRegistry = TextExtractorRegistry(
        listOf(
            PlainTextExtractor(),
            PdfTextExtractor(),
        ),
    ),
) {
    fun load(inputDir: Path): List<RawDocument> {
        InputDirectoryValidator.validate(inputDir)

        return fileScanner.scan(inputDir).mapNotNull { file ->
            toRawDocument(file)
        }
    }

    /**
     * Преобразует найденный файл в `RawDocument`.
     *
     * Повторно определяет `sourceType`, хотя файл уже прошёл фильтрацию в `FileScanner`,
     * чтобы не зависеть от внешних допущений и не падать жёстко при возможном рассинхроне
     * между сканированием и загрузкой. Если тип вдруг не определился, файл пропускается,
     * а подробность уходит в debug-лог для отладки.
     */
    private fun toRawDocument(file: Path): RawDocument? {
        val sourceType = SourceTypeDetector.detect(file) ?: run {
            logger.fine("Пропущен неподдерживаемый файл при загрузке: ${file.toAbsolutePath()}")
            return null
        }
        val extractor = extractorRegistry.getFor(sourceType)
        val extractedText = extractor.extract(file, sourceType)

        return RawDocument(
            documentId = file.toAbsolutePath().normalize().invariantSeparatorsPathString,
            filePath = file.toAbsolutePath().normalize().invariantSeparatorsPathString,
            fileName = file.name,
            sourceType = sourceType,
            title = file.nameWithoutExtension.ifBlank { file.name },
            extension = file.extension.lowercase(),
            text = TextNormalizer.normalize(extractedText),
        )
    }

    private companion object {
        private val logger: Logger = Logger.getLogger(DocumentLoader::class.java.name)
    }
}
