package ru.compadre.indexer.search

import kotlin.math.sqrt

/**
 * Утилита вычисления cosine similarity между двумя embedding-векторами.
 */
object CosineSimilarity {
    fun calculate(left: List<Float>, right: List<Float>): Double {
        require(left.isNotEmpty()) { "Левый вектор не должен быть пустым." }
        require(right.isNotEmpty()) { "Правый вектор не должен быть пустым." }
        require(left.size == right.size) { "Векторы должны быть одной размерности." }

        var dotProduct = 0.0
        var leftNorm = 0.0
        var rightNorm = 0.0

        left.indices.forEach { index ->
            val leftValue = left[index].toDouble()
            val rightValue = right[index].toDouble()

            dotProduct += leftValue * rightValue
            leftNorm += leftValue * leftValue
            rightNorm += rightValue * rightValue
        }

        val denominator = sqrt(leftNorm) * sqrt(rightNorm)
        return if (denominator == 0.0) {
            0.0
        } else {
            dotProduct / denominator
        }
    }
}
