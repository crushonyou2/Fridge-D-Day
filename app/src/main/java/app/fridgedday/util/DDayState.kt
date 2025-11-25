package app.fridgedday.util

enum class DDayState {
    SAFE,      // 7일 초과
    WARNING,   // 2~7일
    EXPIRED    // 1일 이하
}

fun getDDayState(daysUntil: Long): DDayState {
    return when {
        daysUntil > 7 -> DDayState.SAFE      // D-8 이상: 녹색
        daysUntil >= 2 -> DDayState.WARNING  // D-2 ~ D-7: 노란색
        else -> DDayState.EXPIRED            // D-1, D-Day, 만료: 빨간색
    }
}
