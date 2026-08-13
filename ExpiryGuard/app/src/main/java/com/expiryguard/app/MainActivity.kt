package com.expiryguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.expiryguard.app.data.repository.ProductRepository
import com.expiryguard.app.domain.engine.ExpiryRuleEngine
import com.expiryguard.app.domain.model.ProductStatus
import com.expiryguard.app.navigation.AppNavigation
import com.expiryguard.app.notification.NotificationHelper
import com.expiryguard.app.ui.theme.ExpiryGuardTheme
import com.expiryguard.app.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var repository: ProductRepository

    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    private val KEY_MONET = booleanPreferencesKey("monet_enabled")
    private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 启动时检查待处理清单，发送通知
        lifecycleScope.launch {
            try {
                val notificationEnabled = dataStore.data.first()[KEY_NOTIFICATION_ENABLED] ?: true
                if (notificationEnabled) {
                    val products = repository.getAllActiveProducts().first()
                    val today = DateUtils.todayTimestamp()

                    // 待处理：今日到期 + 可退货 + 预警（还剩1天）
                    val todayExpiry = products.filter {
                        DateUtils.isToday(it.expiryDate) && !it.isCompleted
                    }
                    val returnable = products.filter { product ->
                        val status = ExpiryRuleEngine.calculateStatus(
                            product.shelfLifeDays, product.expiryDate
                        )
                        status is ProductStatus.Returnable && !product.isCompleted
                    }
                    val warning = products.filter {
                        val days = DateUtils.daysBetween(today, it.expiryDate)
                        days == 1 && !it.isCompleted
                    }

                    // 合并去重
                    val pendingIds = (todayExpiry.map { it.id } +
                            returnable.map { it.id } +
                            warning.map { it.id }).toSet()

                    val pendingCount = pendingIds.size
                    val returnableCount = returnable.size
                    val expiredCount = products.count {
                        DateUtils.daysBetween(today, it.expiryDate) <= 0 && !it.isCompleted
                    }

                    if (pendingCount > 0) {
                        NotificationHelper.sendPendingNotification(
                            this@MainActivity,
                            pendingCount,
                            returnableCount,
                            expiredCount
                        )
                    }
                }
            } catch (_: Exception) {
                // 忽略启动时的通知错误
            }
        }

        val darkModeFlow = dataStore.data.map { preferences ->
            preferences[KEY_DARK_MODE] ?: false
        }

        val monetFlow = dataStore.data.map { preferences ->
            preferences[KEY_MONET] ?: false
        }

        setContent {
            val isDarkMode by darkModeFlow.collectAsState(initial = false)
            val isMonetEnabled by monetFlow.collectAsState(initial = false)

            ExpiryGuardTheme(
                darkTheme = isDarkMode,
                monetEnabled = isMonetEnabled
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}