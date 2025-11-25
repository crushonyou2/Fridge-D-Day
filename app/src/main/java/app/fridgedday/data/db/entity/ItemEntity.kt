package app.fridgedday.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "items",
    indices = [Index("expiryDate")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String? = null,
    val location: StorageLocation,
    val quantity: Float? = null,
    val unit: String? = null,
    val expiryDate: LocalDate,
    val daysBeforeNotify: Int = 3,
    val note: String? = null,
    val isArchived: Boolean = false,
    val consumedDate: LocalDate? = null,  // 소비 완료한 날짜
    val createdDate: LocalDate = LocalDate.now()  // 등록 날짜
)

enum class StorageLocation {
    FRIDGE,    // 냉장
    FREEZER,   // 냉동
    PANTRY     // 실온
}
