package app.fridgedday.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.pref.SettingsDataStore
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.data.repo.SettingsRepository
import app.fridgedday.util.DateUtils
import app.fridgedday.util.NotificationUtils
import kotlinx.coroutines.flow.first

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get repositories
            val database = AppDatabase.getDatabase(applicationContext)
            val itemRepository = ItemRepository(database.itemDao())
            val settingsDataStore = SettingsDataStore(applicationContext)
            val settingsRepository = SettingsRepository(settingsDataStore)

            // Get settings
            val settings = settingsRepository.settings.first()

            if (!settings.dailyNotify) {
                return Result.success()
            }

            // Get all items
            val allItems = itemRepository.observeAll().first()
            // Filter expiring items (within threshold but not expired)
            val expiringItems = allItems.filter { item ->
                val days = DateUtils.daysUntil(item.expiryDate)
                days in 1..settings.defaultDaysBefore
            }

            // Filter expired items (today or past)
            val expiredItems = allItems.filter { item ->
                DateUtils.daysUntil(item.expiryDate) <= 0
            }

            // Send notifications
            NotificationUtils.sendExpiringSoonNotification(
                applicationContext,
                expiringItems.size
            )

            NotificationUtils.sendExpiredNotification(
                applicationContext,
                expiredItems.size
            )

            // Schedule next check
            WorkScheduler.scheduleDailyCheck(applicationContext, settings)

            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
