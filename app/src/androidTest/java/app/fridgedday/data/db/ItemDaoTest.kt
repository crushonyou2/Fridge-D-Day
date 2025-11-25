package app.fridgedday.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.fridgedday.data.db.dao.ItemDao
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        itemDao = database.itemDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetItem() = runBlocking {
        val item = ItemEntity(
            name = "Milk",
            location = StorageLocation.FRIDGE,
            expiryDate = LocalDate.now().plusDays(7)
        )
        val id = itemDao.insert(item)
        val retrieved = itemDao.getById(id)

        assertEquals("Milk", retrieved?.name)
        assertEquals(StorageLocation.FRIDGE, retrieved?.location)
    }

    @Test
    fun updateItem() = runBlocking {
        val item = ItemEntity(
            name = "Bread",
            location = StorageLocation.PANTRY,
            expiryDate = LocalDate.now().plusDays(3)
        )
        val id = itemDao.insert(item)

        val updated = item.copy(id = id, name = "Fresh Bread")
        itemDao.update(updated)

        val retrieved = itemDao.getById(id)
        assertEquals("Fresh Bread", retrieved?.name)
    }

    @Test
    fun deleteItem() = runBlocking {
        val item = ItemEntity(
            name = "Cheese",
            location = StorageLocation.FRIDGE,
            expiryDate = LocalDate.now().plusDays(10)
        )
        val id = itemDao.insert(item)
        itemDao.delete(item.copy(id = id))

        val retrieved = itemDao.getById(id)
        assertEquals(null, retrieved)
    }

    @Test
    fun observeAllItems() = runBlocking {
        val item1 = ItemEntity(
            name = "Apple",
            location = StorageLocation.FRIDGE,
            expiryDate = LocalDate.now().plusDays(5)
        )
        val item2 = ItemEntity(
            name = "Pasta",
            location = StorageLocation.PANTRY,
            expiryDate = LocalDate.now().plusDays(30)
        )

        itemDao.insert(item1)
        itemDao.insert(item2)

        val items = itemDao.observeAll().first()
        assertEquals(2, items.size)
    }

    @Test
    fun searchByName() = runBlocking {
        itemDao.insert(
            ItemEntity(
                name = "Whole Milk",
                location = StorageLocation.FRIDGE,
                expiryDate = LocalDate.now().plusDays(7)
            )
        )
        itemDao.insert(
            ItemEntity(
                name = "Almond Milk",
                location = StorageLocation.FRIDGE,
                expiryDate = LocalDate.now().plusDays(14)
            )
        )
        itemDao.insert(
            ItemEntity(
                name = "Bread",
                location = StorageLocation.PANTRY,
                expiryDate = LocalDate.now().plusDays(3)
            )
        )

        val results = itemDao.search("milk").first()
        assertEquals(2, results.size)
        assertTrue(results.all { item -> item.name.contains("Milk", ignoreCase = true) })
    }

    @Test
    fun archiveItem() = runBlocking {
        val item = ItemEntity(
            name = "Old Item",
            location = StorageLocation.FREEZER,
            expiryDate = LocalDate.now().minusDays(5),
            isArchived = false
        )
        val id = itemDao.insert(item)

        itemDao.update(item.copy(id = id, isArchived = true))

        val retrieved = itemDao.getById(id)
        assertEquals(true, retrieved?.isArchived)
    }
}
