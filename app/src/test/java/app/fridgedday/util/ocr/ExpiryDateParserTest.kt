package app.fridgedday.util.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExpiryDateParserTest {

    private val today = LocalDate.of(2026, 7, 14)

    @Test
    fun `four digit year date has highest confidence`() {
        val results = ExpiryDateParser.extractDates("유통기한 2026.09.29 까지", today)

        assertTrue(results.contains(ExpiryDateParser.DateResult(LocalDate.of(2026, 9, 29), 3)))
    }

    @Test
    fun `compact date is parsed`() {
        val results = ExpiryDateParser.extractDates("EXP 20261231", today)

        assertTrue(results.contains(ExpiryDateParser.DateResult(LocalDate.of(2026, 12, 31), 3)))
    }

    @Test
    fun `day first date with four digit year is parsed`() {
        val results = ExpiryDateParser.extractDates("USE BY 31/12/2026", today)

        assertTrue(results.contains(ExpiryDateParser.DateResult(LocalDate.of(2026, 12, 31), 3)))
    }

    @Test
    fun `day first date with two digit year is not treated as year first`() {
        val evaluationDate = LocalDate.of(2016, 6, 20)
        val results = ExpiryDateParser.extractDates("USE BY 20.07.16", evaluationDate)

        assertTrue(results.contains(ExpiryDateParser.DateResult(LocalDate.of(2016, 7, 20), 2)))
        assertEquals(LocalDate.of(2016, 7, 20), ExpiryDateParser.selectBestDate(results, evaluationDate))
    }

    @Test
    fun `month and day before today are inferred as next year`() {
        val results = ExpiryDateParser.extractDates("06.30", today)

        assertTrue(results.contains(ExpiryDateParser.DateResult(LocalDate.of(2027, 6, 30), 1)))
    }

    @Test
    fun `invalid calendar date is rejected`() {
        val results = ExpiryDateParser.extractDates("2026.02.30", today)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `best date prefers high confidence over a closer inferred date`() {
        val candidates = listOf(
            ExpiryDateParser.DateResult(LocalDate.of(2026, 7, 15), 1),
            ExpiryDateParser.DateResult(LocalDate.of(2026, 8, 1), 3),
        )

        assertEquals(LocalDate.of(2026, 8, 1), ExpiryDateParser.selectBestDate(candidates, today))
    }
}
