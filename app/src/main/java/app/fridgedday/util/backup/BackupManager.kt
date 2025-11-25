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

    private const val BACKUP_VERSION = 1
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
            val jsonObject = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("exportDate", LocalDate.now().format(dateFormatter))
                put("itemCount", items.size)

                val itemsArray = JSONArray()
                items.forEach { item ->
                    val itemJson = JSONObject().apply {
                        put("name", item.name)
                        put("category", item.category ?: "")
                        put("location", item.location.name)
                        put("quantity", item.quantity ?: 0f)
                        put("unit", item.unit ?: "")
                        put("expiryDate", item.expiryDate.format(dateFormatter))
                        put("daysBeforeNotify", item.daysBeforeNotify)
                        put("note", item.note ?: "")
                    }
                    itemsArray.put(itemJson)
                }
                put("items", itemsArray)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonObject.toString(2).toByteArray())
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

            val jsonObject = JSONObject(jsonString)
            val version = jsonObject.optInt("version", 1)

            if (version > BACKUP_VERSION) {
                throw Exception("지원하지 않는 백업 버전입니다")
            }

            val itemsArray = jsonObject.getJSONArray("items")
            val items = mutableListOf<ItemEntity>()

            for (i in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(i)

                val item = ItemEntity(
                    name = itemJson.getString("name"),
                    category = itemJson.optString("category").takeIf { it.isNotEmpty() },
                    location = StorageLocation.valueOf(itemJson.getString("location")),
                    quantity = itemJson.optDouble("quantity").takeIf { it != 0.0 }?.toFloat(),
                    unit = itemJson.optString("unit").takeIf { it.isNotEmpty() },
                    expiryDate = LocalDate.parse(itemJson.getString("expiryDate"), dateFormatter),
                    daysBeforeNotify = itemJson.optInt("daysBeforeNotify", 3),
                    note = itemJson.optString("note").takeIf { it.isNotEmpty() }
                )
                items.add(item)
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
