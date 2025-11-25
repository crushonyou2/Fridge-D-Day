package app.fridgedday.data.pref

import java.time.LocalTime

data class AppSettings(
    val dailyNotify: Boolean = true,
    val notifyTime: LocalTime = LocalTime.of(9, 0),
    val defaultDaysBefore: Int = 3,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isFirstLaunch: Boolean = true
)

enum class ThemeMode {
    SYSTEM,  // 시스템 설정 따르기
    LIGHT,   // 라이트 모드
    DARK     // 다크 모드
}
