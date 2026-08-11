package app.fridgedday.util.backup

import android.content.Context
import android.net.Uri
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BackupManager {

    private const val BACKUP_VERSION = 2
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * 아이템 목록을 JSON으로 백업
     */
    suspend fun exportToJson(
        context: Context,
        items: List<ItemEntity>,
        uri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(serialize(items).toByteArray())
            } ?: throw Exception("파일을 열 수 없습니다")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * JSON 파일에서 아이템 목록 복원
     */
    suspend fun importFromJson(
        context: Context,
        uri: Uri
    ): Result<List<ItemEntity>> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw Exception("파일을 읽을 수 없습니다")

            Result.success(deserialize(jsonString))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun serialize(
        items: List<ItemEntity>,
        exportDate: LocalDate = LocalDate.now()
    ): String {
        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportDate", exportDate.format(dateFormatter))
            put("itemCount", items.size)

            val itemsArray = JSONArray()
            items.forEach { item ->
                itemsArray.put(
                    JSONObject().apply {
                        put("name", item.name)
                        put("category", item.category ?: JSONObject.NULL)
                        put("location", item.location.name)
                        put("quantity", item.quantity ?: JSONObject.NULL)
                        put("unit", item.unit ?: JSONObject.NULL)
                        put("expiryDate", item.expiryDate.format(dateFormatter))
                        put("daysBeforeNotify", item.daysBeforeNotify)
                        put("note", item.note ?: JSONObject.NULL)
                        put("isArchived", item.isArchived)
                        put(
                            "consumedDate",
                            item.consumedDate?.format(dateFormatter) ?: JSONObject.NULL
                        )
                        put("createdDate", item.createdDate.format(dateFormatter))
                    }
                )
            }
            put("items", itemsArray)
        }.toString(2)
    }

    internal fun deserialize(
        jsonString: String,
        fallbackCreatedDate: LocalDate = LocalDate.now()
    ): List<ItemEntity> {
        val jsonObject = JSONObject(jsonString)
        val version = jsonObject.optInt("version", 1)
        require(version in 1..BACKUP_VERSION) {
            "지원하지 않는 백업 버전입니다"
        }

        val itemsArray = jsonObject.getJSONArray("items")
        return buildList {
            for (index in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(index)
                add(
                    ItemEntity(
                        name = itemJson.getString("name"),
                        category = itemJson.nullableString("category"),
                        location = StorageLocation.valueOf(itemJson.getString("location")),
                        quantity = if (itemJson.isNull("quantity")) {
                            null
                        } else {
                            itemJson.getDouble("quantity").toFloat().takeIf {
                                version >= 2 || it != 0f
                            }
                        },
                        unit = itemJson.nullableString("unit"),
                        expiryDate = LocalDate.parse(
                            itemJson.getString("expiryDate"),
                            dateFormatter
                        ),
                        daysBeforeNotify = itemJson.optInt("daysBeforeNotify", 3),
                        note = itemJson.nullableString("note"),
                        isArchived = if (version >= 2) {
                            itemJson.optBoolean("isArchived", false)
                        } else {
                            false
                        },
                        consumedDate = if (version >= 2) {
                            itemJson.nullableString("consumedDate")?.let {
                                LocalDate.parse(it, dateFormatter)
                            }
                        } else {
                            null
                        },
                        createdDate = if (version >= 2) {
                            itemJson.nullableString("createdDate")?.let {
                                LocalDate.parse(it, dateFormatter)
                            } ?: fallbackCreatedDate
                        } else {
                            fallbackCreatedDate
                        }
                    )
                )
            }
        }
    }

    private fun JSONObject.nullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return getString(key).takeIf { it.isNotEmpty() }
    }
}
