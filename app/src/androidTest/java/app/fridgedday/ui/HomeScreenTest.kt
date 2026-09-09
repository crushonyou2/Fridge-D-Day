package app.fridgedday.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.fridgedday.FridgeDDayTheme
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.pref.SettingsDataStore
import app.fridgedday.ui.home.HomeScreen
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun resetAppState() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AppDatabase.getDatabase(context).clearAllTables()
        SettingsDataStore(context).setFirstLaunchCompleted()
    }

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
            .onNodeWithText("등록된 식품이 없어요")
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

    @Test
    fun homeScreen_emptyStateShowsFirstItemCta() {
        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("첫 식품 등록")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun searchWithExistingItems_showsNoResultsStateInsteadOfTrueEmptyState() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AppDatabase.getDatabase(context).itemDao().insert(
            ItemEntity(
                name = "우유",
                location = StorageLocation.FRIDGE,
                expiryDate = LocalDate.now().plusDays(7)
            )
        )

        composeTestRule.setContent {
            FridgeDDayTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("우유").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("검색").performClick()
        composeTestRule.onNodeWithText("이름으로 검색").performTextInput("없는식품")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("조건에 맞는 식품이 없어요")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("조건에 맞는 식품이 없어요").assertIsDisplayed()
        composeTestRule.onNodeWithText("필터 초기화").assertHasClickAction()
        composeTestRule.onNodeWithText("등록된 식품이 없어요").assertDoesNotExist()
    }
}
