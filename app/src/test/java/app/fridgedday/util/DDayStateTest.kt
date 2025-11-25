package app.fridgedday.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DDayStateTest {

    @Test
    fun `getDDayState returns SAFE for more than 7 days`() {
        val result = getDDayState(daysUntil = 10)
        assertEquals(DDayState.SAFE, result)
    }

    @Test
    fun `getDDayState returns WARNING for 1 to 7 days`() {
        val result1 = getDDayState(daysUntil = 7)
        val result2 = getDDayState(daysUntil = 1)

        assertEquals(DDayState.WARNING, result1)
        assertEquals(DDayState.WARNING, result2)
    }

    @Test
    fun `getDDayState returns EXPIRED for 0 or negative days`() {
        val result1 = getDDayState(daysUntil = 0)
        val result2 = getDDayState(daysUntil = -5)

        assertEquals(DDayState.EXPIRED, result1)
        assertEquals(DDayState.EXPIRED, result2)
    }
}
