package app.fridgedday.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.fridgedday.FridgeDDayTheme
import app.fridgedday.ui.home.HomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysTitle() {
        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("오늘도 신선")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysEmptyState() {
        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("아직 항목이 없어요")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_hasFAB() {
        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("추가")
            .assertIsDisplayed()
    }

    @Test
    fun searchButton_togglesSearchField() {
        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        // Search icon button exists
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertIsDisplayed()

        // Click search button to show search field
        composeTestRule
            .onNodeWithContentDescription("검색")
            .performClick()

        // Now search field should be visible
        composeTestRule
            .onNodeWithText("이름으로 검색")
            .assertIsDisplayed()
    }
}
