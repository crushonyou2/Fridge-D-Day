package app.fridgedday.ui.addedit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.fridgedday.FridgeDDayTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class OcrDateConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recognizedDateRequiresExplicitConfirmation() {
        var action = ""
        val recognizedDate = LocalDate.of(2026, 8, 20)

        composeTestRule.setContent {
            FridgeDDayTheme {
                OcrDateConfirmationDialog(
                    recognizedDate = recognizedDate,
                    onConfirm = { action = "confirm" },
                    onEdit = { action = "edit" },
                    onCancel = { action = "cancel" }
                )
            }
        }

        composeTestRule.onNodeWithText("인식된 날짜가 맞나요?").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026-08-20").assertIsDisplayed()
        assertEquals("", action)

        composeTestRule.onNodeWithText("이 날짜 확인").performClick()
        assertEquals("confirm", action)
    }

    @Test
    fun recognizedDateCanBeEdited() {
        var edited = false

        composeTestRule.setContent {
            FridgeDDayTheme {
                OcrDateConfirmationDialog(
                    recognizedDate = LocalDate.of(2026, 8, 20),
                    onConfirm = {},
                    onEdit = { edited = true },
                    onCancel = {}
                )
            }
        }

        composeTestRule.onNodeWithText("수정").performClick()
        assertEquals(true, edited)
    }

    @Test
    fun recognizedDateCanBeCancelled() {
        var cancelled = false

        composeTestRule.setContent {
            FridgeDDayTheme {
                OcrDateConfirmationDialog(
                    recognizedDate = LocalDate.of(2026, 8, 20),
                    onConfirm = {},
                    onEdit = {},
                    onCancel = { cancelled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("취소").performClick()
        assertEquals(true, cancelled)
    }
}
