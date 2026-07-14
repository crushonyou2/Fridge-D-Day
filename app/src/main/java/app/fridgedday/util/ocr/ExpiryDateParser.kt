package app.fridgedday.util.ocr

import java.time.LocalDate
import java.util.regex.Pattern
import kotlin.math.abs

/** Android·ML Kit과 분리된 순수 날짜 파서. 로컬 단위 테스트에서 재현할 수 있다. */
internal object ExpiryDateParser {

    data class DateResult(val date: LocalDate, val confidence: Int)

    fun extractDates(rawText: String, today: LocalDate = LocalDate.now()): List<DateResult> {
        val results = mutableListOf<DateResult>()
        val text = rawText.replace("\n", " ")

        val processedText = text.uppercase()
            .replace("SIMPLUS", "")
            .replace("1A", "")
            .replace("1. :", "11")
            .replace("1. 1", "11")
            .replace("A", "9")
            .replace("O", "0")
            .replace(":", "1")

        val pattern8 = Pattern.compile("(\\d{4})[.\\- /]+(\\d{1,2})[.\\- /]+(\\d{1,2})")
        var matcher = pattern8.matcher(processedText)
        while (matcher.find()) {
            addDate(results, matcher.group(1)!!.toInt(), matcher.group(2)!!.toInt(), matcher.group(3)!!.toInt(), 3)
        }

        val pattern6 = Pattern.compile("(?<!\\d)(\\d{2})[.\\- /]+(\\d{1,2})[.\\- /]+(\\d{1,2})")
        matcher = pattern6.matcher(processedText)
        while (matcher.find()) {
            addDate(results, 2000 + matcher.group(1)!!.toInt(), matcher.group(2)!!.toInt(), matcher.group(3)!!.toInt(), 2)
        }

        val pattern4 = Pattern.compile("(?<!\\d|\\.)(\\d{1,2})[.\\- /]+(\\d{1,2})(?!\\d|\\.)")
        matcher = pattern4.matcher(processedText)
        while (matcher.find()) {
            val month = matcher.group(1)!!.toInt()
            val day = matcher.group(2)!!.toInt()
            if (isValidDate(today.year, month, day)) {
                val currentYearDate = LocalDate.of(today.year, month, day)
                val inferredDate = if (currentYearDate.isBefore(today.minusDays(1))) currentYearDate.plusYears(1) else currentYearDate
                results.add(DateResult(inferredDate, 1))
            }
        }

        val digitsOnly = processedText.filter { it.isDigit() }
        if (digitsOnly.length >= 8) {
            for (i in 0..digitsOnly.length - 8) {
                val candidate = digitsOnly.substring(i, i + 8)
                addDate(results, candidate.substring(0, 4).toInt(), candidate.substring(4, 6).toInt(), candidate.substring(6, 8).toInt(), 3)
            }
        }
        if (digitsOnly.length >= 6) {
            for (i in 0..digitsOnly.length - 6) {
                val candidate = digitsOnly.substring(i, i + 6)
                addDate(results, 2000 + candidate.substring(0, 2).toInt(), candidate.substring(2, 4).toInt(), candidate.substring(4, 6).toInt(), 2)
            }
        }

        return results.distinct()
    }

    fun selectBestDate(candidates: List<DateResult>, today: LocalDate = LocalDate.now()): LocalDate? {
        val validCandidates = candidates.filter {
            it.date.isAfter(today.minusYears(1)) && it.date.isBefore(today.plusYears(5))
        }
        if (validCandidates.isEmpty()) return null

        val highConfidence = validCandidates.filter { it.confidence >= 2 }
        val pool = highConfidence.ifEmpty { validCandidates }
        return pool.minByOrNull { abs(it.date.toEpochDay() - today.toEpochDay()) }?.date
    }

    private fun addDate(results: MutableList<DateResult>, year: Int, month: Int, day: Int, confidence: Int) {
        if (isValidDate(year, month, day)) {
            results.add(DateResult(LocalDate.of(year, month, day), confidence))
        }
    }

    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        if (month !in 1..12 || day !in 1..31) return false
        return runCatching { LocalDate.of(year, month, day) }.isSuccess
    }
}
