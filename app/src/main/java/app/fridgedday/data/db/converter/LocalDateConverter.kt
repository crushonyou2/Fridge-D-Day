package app.fridgedday.data.db.converter

import androidx.room.TypeConverter
import java.time.LocalDate

object LocalDateConverter {
    @TypeConverter
    fun toString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromString(str: String?): LocalDate? {
        return str?.let { LocalDate.parse(it) }
    }
}
