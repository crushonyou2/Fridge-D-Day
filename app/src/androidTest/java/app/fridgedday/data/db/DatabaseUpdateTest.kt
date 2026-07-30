package app.fridgedday.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseUpdateTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Before
    fun clearDatabase() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @After
    fun cleanupDatabase() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    @Throws(IOException::class)
    fun versionTwoDatabaseOpensWithoutDataLoss() = runBlocking {
        migrationHelper.createDatabase(TEST_DATABASE, 2).apply {
            execSQL(
                """
                INSERT INTO items (
                    id, name, category, location, quantity, unit, expiryDate,
                    daysBeforeNotify, note, isArchived, consumedDate, createdDate
                ) VALUES (
                    42, '업데이트 보존 식품', NULL, 'FRIDGE', 1.0, '개',
                    '2026-08-20', 3, NULL, 0, NULL, '2026-07-31'
                )
                """.trimIndent()
            )
            close()
        }

        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DATABASE
        ).build()

        try {
            val item = database.itemDao().getById(42)
            assertNotNull(item)
            assertEquals("업데이트 보존 식품", item?.name)
            assertEquals("2026-08-20", item?.expiryDate.toString())
            assertEquals("2026-07-31", item?.createdDate.toString())
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEST_DATABASE = "code-2-to-code-3-test"
    }
}
