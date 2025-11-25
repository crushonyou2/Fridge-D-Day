package app.fridgedday.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun `daysUntil returns positive for future dates`() {
        val futureDate = LocalDate.now().plusDays(5)
        val result = DateUtils.daysUntil(futureDate)
        assertEquals(5L, result)
    }

    @Test
    fun `daysUntil returns zero for today`() {
        val today = LocalDate.now()
        val result = DateUtils.daysUntil(today)
        assertEquals(0L, result)
    }

    @Test
    fun `daysUntil returns negative for past dates`() {
        val pastDate = LocalDate.now().minusDays(3)
        val result = DateUtils.daysUntil(pastDate)
        assertEquals(-3L, result)
    }

    @Test
    fun `formatDDay shows D-n for future dates`() {
        val futureDate = LocalDate.now().plusDays(7)
        val result = DateUtils.formatDDay(futureDate)
        assertEquals("D-7", result)
    }

    @Test
    fun `formatDDay shows D-Day for today`() {
        val today = LocalDate.now()
        val result = DateUtils.formatDDay(today)
        assertEquals("D-Day", result)
    }

    @Test
    fun `formatDDay shows D+n for past dates`() {
        val pastDate = LocalDate.now().minusDays(2)
        val result = DateUtils.formatDDay(pastDate)
        assertEquals("D+2", result)
    }

    @Test
    fun `formatKorean returns correct format`() {
        val date = LocalDate.of(2025, 12, 31)
        val result = DateUtils.formatKorean(date)
        assertEquals("2025년 12월 31일", result)
    }
}
