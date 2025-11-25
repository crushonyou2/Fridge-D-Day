package app.fridgedday.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.fridgedday.data.db.converter.LocalDateConverter
import app.fridgedday.data.db.converter.StorageLocationConverter
import app.fridgedday.data.db.dao.ItemDao
import app.fridgedday.data.db.entity.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    StorageLocationConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fridgedday_database"
                )
                .fallbackToDestructiveMigration()  // 개발 중이므로 데이터 삭제하고 재생성
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
