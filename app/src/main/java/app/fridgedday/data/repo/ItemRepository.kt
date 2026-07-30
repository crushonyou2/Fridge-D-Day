package app.fridgedday.data.repo

import app.fridgedday.data.db.dao.ItemDao
import app.fridgedday.data.db.entity.ItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AddEditItemRepository {
    suspend fun getById(id: Long): ItemEntity?
    suspend fun insert(item: ItemEntity): Long
    suspend fun update(item: ItemEntity)
}

class ItemRepository(
    private val itemDao: ItemDao
) : AddEditItemRepository {

    fun observeAll(): Flow<List<ItemEntity>> = itemDao.observeAll()

    fun search(keyword: String): Flow<List<ItemEntity>> = itemDao.search(keyword)

    override suspend fun getById(id: Long): ItemEntity? = itemDao.getById(id)

    override suspend fun insert(item: ItemEntity): Long = itemDao.insert(item)

    override suspend fun update(item: ItemEntity) = itemDao.update(item)

    suspend fun archive(id: Long) = itemDao.archive(id)

    suspend fun markConsumed(id: Long) = itemDao.markConsumed(id, LocalDate.now())

    suspend fun delete(item: ItemEntity) = itemDao.delete(item)

    suspend fun getAllItems(): List<ItemEntity> = itemDao.getAllItems()

    suspend fun getDueItems(daysBefore: Int): List<ItemEntity> {
        val targetDate = LocalDate.now().plusDays(daysBefore.toLong())
        return itemDao.dueBefore(targetDate)
    }

    suspend fun getExpiredItems(): List<ItemEntity> {
        return itemDao.dueBefore(LocalDate.now())
    }
}
