package ru.compadre.indexer.loader

import ru.compadre.indexer.extractor.PdfTextExtractor
import ru.compadre.indexer.extractor.PlainTextExtractor
import ru.compadre.indexer.extractor.TextExtractorRegistry
import ru.compadre.indexer.extractor.TextNormalizer
import ru.compadre.indexer.model.RawDocument
import java.nio.file.Path
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

        return fileScanner.scan(inputDir).map { file ->
            toRawDocument(file)
        }
    }

    private fun toRawDocument(file: Path): RawDocument {
        val sourceType = requireNotNull(SourceTypeDetector.detect(file)) {
            "Файл `${file.toAbsolutePath()}` не поддерживается загрузчиком."
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
}
