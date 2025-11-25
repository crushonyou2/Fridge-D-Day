package app.fridgedday.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateUtils {
    /**
     * 오늘부터 목표 날짜까지 남은 일수 계산
     * 양수: 미래, 0: 오늘, 음수: 과거
     */
    fun daysUntil(expiryDate: LocalDate): Long {
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, expiryDate)
    }

    /**
     * D-Day 문자열 생성
     * 예: D-3, D-Day, D+2
     */
    fun formatDDay(expiryDate: LocalDate): String {
        val days = daysUntil(expiryDate)
        return when {
            days > 0 -> "D-$days"
            days == 0L -> "D-Day"
            else -> "D+${-days}"
        }
    }

    /**
     * 날짜를 한국어 형식으로 포맷
     * 예: 2025-12-31 → 2025년 12월 31일
     */
    fun formatKorean(date: LocalDate): String {
        return "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
    }
}
