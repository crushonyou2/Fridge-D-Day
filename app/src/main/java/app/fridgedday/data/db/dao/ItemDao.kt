package app.fridgedday.data.db.dao

import androidx.room.*
import app.fridgedday.data.db.entity.ItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isArchived = 0 ORDER BY expiryDate ASC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("""
        SELECT * FROM items
        WHERE isArchived = 0
          AND expiryDate <= :toDate
        ORDER BY expiryDate ASC
    """)
    suspend fun dueBefore(toDate: LocalDate): List<ItemEntity>

    @Query("""
        SELECT * FROM items
        WHERE isArchived = 0
          AND name LIKE '%' || :keyword || '%'
        ORDER BY expiryDate ASC
    """)
    fun search(keyword: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): ItemEntity?

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Query("UPDATE items SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE items SET isArchived = 1, consumedDate = :consumedDate WHERE id = :id")
    suspend fun markConsumed(id: Long, consumedDate: LocalDate)

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ItemEntity>

    @Delete
    suspend fun delete(item: ItemEntity)
}
