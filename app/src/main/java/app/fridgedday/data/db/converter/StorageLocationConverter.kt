package app.fridgedday.data.db.converter

import androidx.room.TypeConverter
import app.fridgedday.data.db.entity.StorageLocation

object StorageLocationConverter {
    @TypeConverter
    fun toString(location: StorageLocation): String {
        return location.name
    }

    @TypeConverter
    fun fromString(str: String): StorageLocation {
        return StorageLocation.valueOf(str)
    }
}
