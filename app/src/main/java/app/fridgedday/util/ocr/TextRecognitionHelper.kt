package app.fridgedday.util.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Matrix
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

object TextRecognitionHelper {

    private const val TAG = "OcrHelper"

    data class OcrEvaluation(
        val selectedDate: LocalDate?,
        val candidateCount: Int,
        val candidateDates: Set<LocalDate>,
        val hasRecognizedText: Boolean,
        val processedVariantCount: Int,
        val failedVariantCount: Int,
    )

    private data class ProcessResult(
        val candidates: List<ExpiryDateParser.DateResult>,
        val hasRecognizedText: Boolean,
    )

    private data class ProcessAttempt(
        val result: ProcessResult?,
        val failed: Boolean,
    )

    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    suspend fun extractExpiryDate(
        bitmap: Bitmap,
        baseRotation: Int = 0,
        today: LocalDate = LocalDate.now(),
    ): LocalDate? = evaluateExpiryDate(bitmap, baseRotation, today).selectedDate

    /** OCR 벤치마크에서 인식·파싱·선택 실패를 분리하기 위한 진단 결과. */
    suspend fun evaluateExpiryDate(
        bitmap: Bitmap,
        baseRotation: Int = 0,
        today: LocalDate = LocalDate.now(),
    ): OcrEvaluation {
        val candidates = mutableListOf<ExpiryDateParser.DateResult>()
        var hasRecognizedText = false
        var processedVariantCount = 0
        var failedVariantCount = 0

        // 1. 원본 이미지
        for (rot in listOf(baseRotation, baseRotation + 90)) {
            val rotated = rotateBitmap(bitmap, rot.toFloat())
            try {
                val attempt = processBitmap(rotated, today)
                if (attempt.failed) failedVariantCount += 1 else processedVariantCount += 1
                attempt.result?.let { result ->
                    candidates.addAll(result.candidates)
                    hasRecognizedText = hasRecognizedText || result.hasRecognizedText
                }
            } finally {
                if (rotated !== bitmap) rotated.recycle()
            }
        }

        // 2. 반전 이미지
        val inverted = applyInvertFilter(bitmap)
        try {
            for (rot in listOf(baseRotation, baseRotation + 90)) {
                val rotated = rotateBitmap(inverted, rot.toFloat())
                try {
                    val attempt = processBitmap(rotated, today)
                    if (attempt.failed) failedVariantCount += 1 else processedVariantCount += 1
                    attempt.result?.let { result ->
                        candidates.addAll(result.candidates)
                        hasRecognizedText = hasRecognizedText || result.hasRecognizedText
                    }
                } finally {
                    if (rotated !== inverted) rotated.recycle()
                }
            }
        } finally {
            inverted.recycle()
        }

        val distinctCandidates = candidates.distinct()
        return OcrEvaluation(
            selectedDate = ExpiryDateParser.selectBestDate(distinctCandidates, today),
            candidateCount = distinctCandidates.size,
            candidateDates = distinctCandidates.mapTo(mutableSetOf()) { it.date },
            hasRecognizedText = hasRecognizedText,
            processedVariantCount = processedVariantCount,
            failedVariantCount = failedVariantCount,
        )
    }

    private suspend fun processBitmap(
        bitmap: Bitmap,
        today: LocalDate,
    ): ProcessAttempt {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            ProcessAttempt(
                result = ProcessResult(
                    candidates = ExpiryDateParser.extractDates(result.text, today),
                    hasRecognizedText = result.text.isNotBlank(),
                ),
                failed = false,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 삼키지 않고 남긴다. 모듈 미준비와 그 밖의 실패를 사후에 구분할 수 있어야 한다.
            Log.w(TAG, "OCR variant failed: ${e.javaClass.simpleName}: ${e.message}")
            ProcessAttempt(result = null, failed = true)
        }
    }

    // --- Utils ---

    private fun applyInvertFilter(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}
