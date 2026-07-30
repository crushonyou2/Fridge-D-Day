package app.fridgedday.worker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class WorkSchedulerTest {

    @Test
    fun futureTimeSchedulesLaterTheSameDay() {
        val now = LocalDateTime.of(2026, 7, 31, 8, 30)

        val delay = WorkScheduler.calculateInitialDelay(now, LocalTime.of(10, 0))

        assertEquals(Duration.ofMinutes(90), delay)
    }

    @Test
    fun pastTimeSchedulesTheNextDay() {
        val now = LocalDateTime.of(2026, 7, 31, 10, 30)

        val delay = WorkScheduler.calculateInitialDelay(now, LocalTime.of(9, 0))

        assertEquals(Duration.ofHours(22).plusMinutes(30), delay)
    }

    @Test
    fun exactTimeSchedulesTheNextDay() {
        val now = LocalDateTime.of(2026, 7, 31, 9, 0)

        val delay = WorkScheduler.calculateInitialDelay(now, LocalTime.of(9, 0))

        assertEquals(Duration.ofDays(1), delay)
    }
}
