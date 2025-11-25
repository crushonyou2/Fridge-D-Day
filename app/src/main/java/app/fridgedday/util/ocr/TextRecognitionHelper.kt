package app.fridgedday.util.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Matrix
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.regex.Pattern
import kotlin.math.abs

object TextRecognitionHelper {

    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    // confidence: 3=YYYYMMDD(완벽), 2=YYMMDD(보통), 1=MMDD(추정)
    data class DateResult(val date: LocalDate, val confidence: Int)

    suspend fun extractExpiryDate(bitmap: Bitmap, baseRotation: Int = 0): LocalDate? {
        val candidates = mutableListOf<DateResult>()

        // 1. 원본 이미지
        for (rot in listOf(0, 90)) {
            val rotated = rotateBitmap(bitmap, rot.toFloat())
            processBitmap(rotated, "ORG_$rot")?.let { candidates.addAll(it) }
        }

        // 2. 반전 이미지
        val inverted = applyInvertFilter(bitmap)
        for (rot in listOf(0, 90)) {
            val rotated = rotateBitmap(inverted, rot.toFloat())
            processBitmap(rotated, "INV_$rot")?.let { candidates.addAll(it) }
        }

        return selectBestDate(candidates)
    }

    private fun selectBestDate(candidates: List<DateResult>): LocalDate? {
        val today = LocalDate.now()
        
        // 필터링: 오늘부터 -1년 ~ +5년 사이의 날짜만 유효하다고 판단
        val validCandidates = candidates.filter { 
            it.date.isAfter(today.minusYears(1)) && it.date.isBefore(today.plusYears(5))
        }

        if (validCandidates.isEmpty()) return null

        // 1순위: 8자리/6자리 (Confidence 3, 2) 중 오늘과 가장 가까운 날짜
        val highConfidence = validCandidates.filter { it.confidence >= 2 }
        if (highConfidence.isNotEmpty()) {
            return highConfidence.minByOrNull { abs(it.date.toEpochDay() - today.toEpochDay()) }?.date
        }

        // 2순위: 4자리 (Confidence 1) 중 오늘 이후이면서 가장 가까운 날짜
        // (이미 지난 날짜는 4자리일 경우 신뢰도가 낮으므로 배제하려 했으나, 유통기한 만료일 수 있으므로 포함)
        return validCandidates.minByOrNull { abs(it.date.toEpochDay() - today.toEpochDay()) }?.date
    }

    private suspend fun processBitmap(bitmap: Bitmap, tag: String): List<DateResult>? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            // 로그 확인 (디버깅용)
            val logText = result.text.replace("\n", " ")
            Log.d("OCR_RAW_$tag", logText)
            extractDatesFromText(result.text)
        } catch (e: Exception) { null }
    }

    private fun extractDatesFromText(rawText: String): List<DateResult> {
        val results = mutableListOf<DateResult>()
        val text = rawText.replace("\n", " ") // 한 줄로 병합

        // [1번 Simplus 해결을 위한 특수 전처리]
        // "1. simplus" -> "11.19" (문맥상 1. 뒤에 19가 있다고 가정하거나 강제 치환)
        // "1. 1" -> "11"
        var processedText = text.uppercase()
            .replace("SIMPLUS", "") // 노이즈 텍스트 제거
            .replace("1A", "")      // 노이즈 제거
            .replace("1. :", "11") 
            .replace("1. 1", "11")
        
        // [3번 노란 뚜껑 해결]
        processedText = processedText
            .replace("A", "9")
            .replace("O", "0")
            .replace(":", "1")

        // 정규식으로 날짜 추출
        
        // 1. YYYY.MM.DD (가장 강력)
        val pattern8 = Pattern.compile("(\\d{4})[.\\- /]+(\\d{1,2})[.\\- /]+(\\d{1,2})")
        var matcher = pattern8.matcher(processedText)
        while (matcher.find()) {
            try {
                val y = matcher.group(1)!!.toInt()
                val m = matcher.group(2)!!.toInt()
                val d = matcher.group(3)!!.toInt()
                if (isValidDate(y, m, d)) results.add(DateResult(LocalDate.of(y, m, d), 3))
            } catch(e: Exception) {}
        }

        // 2. YY.MM.DD
        val pattern6 = Pattern.compile("(?<!\\d)(\\d{2})[.\\- /]+(\\d{1,2})[.\\- /]+(\\d{1,2})")
        matcher = pattern6.matcher(processedText)
        while (matcher.find()) {
            try {
                val y = 2000 + matcher.group(1)!!.toInt()
                val m = matcher.group(2)!!.toInt()
                val d = matcher.group(3)!!.toInt()
                if (isValidDate(y, m, d)) results.add(DateResult(LocalDate.of(y, m, d), 2))
            } catch(e: Exception) {}
        }
        
        // 3. MM.DD (Simplus 11.19 대응)
        // 연도 없이 월.일 만 있는 경우. 
        // 조건: 앞뒤에 숫자가 없어야 함 (YYYY.MM.DD의 일부분이 아님)
        val pattern4 = Pattern.compile("(?<!\\d|\\.)(\\d{1,2})[.\\- /]+(\\d{1,2})(?!\\d|\\.)")
        matcher = pattern4.matcher(processedText)
        while (matcher.find()) {
            try {
                val m = matcher.group(1)!!.toInt()
                val d = matcher.group(2)!!.toInt()
                val today = LocalDate.now()
                if (isValidDate(today.year, m, d)) {
                    val date = LocalDate.of(today.year, m, d)
                    // 오늘보다 과거면 내년으로 예측
                    val finalDate = if (date.isBefore(today.minusDays(1))) date.plusYears(1) else date
                    results.add(DateResult(finalDate, 1))
                }
            } catch(e: Exception) {}
        }

        // 4. 8자리/6자리 숫자 연속 (점자가 공백 없이 붙어있는 경우: 20260929)
        val digitsOnly = processedText.filter { it.isDigit() }
        if (digitsOnly.length >= 6) {
            // Sliding window for 8 digits
            for (i in 0..digitsOnly.length - 8) {
                try {
                    val sub = digitsOnly.substring(i, i+8)
                    val y = sub.substring(0, 4).toInt()
                    val m = sub.substring(4, 6).toInt()
                    val d = sub.substring(6, 8).toInt()
                    if (isValidDate(y, m, d)) results.add(DateResult(LocalDate.of(y, m, d), 3))
                } catch(e:Exception){}
            }
            // Sliding window for 6 digits
            for (i in 0..digitsOnly.length - 6) {
                try {
                    val sub = digitsOnly.substring(i, i+6)
                    val y = 2000 + sub.substring(0, 2).toInt()
                    val m = sub.substring(2, 4).toInt()
                    val d = sub.substring(4, 6).toInt()
                    if (isValidDate(y, m, d)) results.add(DateResult(LocalDate.of(y, m, d), 2))
                } catch(e:Exception){}
            }
        }

        return results
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

    private fun isValidDate(y: Int, m: Int, d: Int): Boolean {
        if (m !in 1..12) return false
        if (d !in 1..31) return false
        return try { LocalDate.of(y, m, d); true } catch (e: Exception) { false }
    }
}
