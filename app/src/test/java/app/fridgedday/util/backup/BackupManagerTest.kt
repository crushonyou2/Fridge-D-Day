package app.fridgedday.util.backup

import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BackupManagerTest {

    @Test
    fun versionTwoRoundTripPreservesItemHistory() {
        val item = ItemEntity(
            id = 9,
            name = "소비한 우유",
            category = "유제품",
            location = StorageLocation.FRIDGE,
            quantity = 0f,
            unit = "개",
            expiryDate = LocalDate.of(2026, 7, 30),
            daysBeforeNotify = 5,
            note = "테스트",
            isArchived = true,
            consumedDate = LocalDate.of(2026, 7, 29),
            createdDate = LocalDate.of(2026, 7, 1)
        )

        val json = BackupManager.serialize(
            items = listOf(item),
            exportDate = LocalDate.of(2026, 7, 31)
        )
        val restored = BackupManager.deserialize(json).single()

        assertEquals(item.name, restored.name)
        assertEquals(item.quantity, restored.quantity)
        assertEquals(item.isArchived, restored.isArchived)
        assertEquals(item.consumedDate, restored.consumedDate)
        assertEquals(item.createdDate, restored.createdDate)
    }

    @Test
    fun versionOneBackupRemainsReadable() {
        val fallbackDate = LocalDate.of(2026, 7, 31)
        val versionOneJson = """
            {
              "version": 1,
              "items": [
                {
                  "name": "기존 백업",
                  "category": "",
                  "location": "PANTRY",
                  "quantity": 0,
                  "unit": "",
                  "expiryDate": "2026-08-20",
                  "daysBeforeNotify": 3,
                  "note": ""
                }
              ]
            }
        """.trimIndent()

        val restored = BackupManager.deserialize(
            versionOneJson,
            fallbackCreatedDate = fallbackDate
        ).single()

        assertNull(restored.category)
        assertNull(restored.quantity)
        assertFalse(restored.isArchived)
        assertNull(restored.consumedDate)
        assertEquals(fallbackDate, restored.createdDate)
    }
}
