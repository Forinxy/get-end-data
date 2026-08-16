package com.expiryguard.app.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expiryguard.app.data.repository.ProductRepository
import com.expiryguard.app.domain.engine.ExpiryRuleEngine
import com.expiryguard.app.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 定时到期提醒 Worker。
 *
 * 周期检查待处理清单（今日到期 + 可退货 + 已过期 + 预警），
 * 在通知开关开启时发送提醒通知。
 */
@HiltWorker
class ExpiryReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ProductRepository,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 通知开关未开启则跳过
            val enabled = dataStore.data.first()[NotificationPrefs.KEY_NOTIFICATION_ENABLED] ?: true
            if (!enabled) return Result.success()

            val products = repository.getAllActiveProducts().first()
            val today = DateUtils.todayTimestamp()
            val groups = ExpiryRuleEngine.computePendingGroups(products, today)

            val pendingCount = groups.totalCount
            if (pendingCount > 0) {
                NotificationHelper.sendPendingNotification(
                    applicationContext,
                    pendingCount,
                    groups.returnable.size,
                    groups.expired.size
                )
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
