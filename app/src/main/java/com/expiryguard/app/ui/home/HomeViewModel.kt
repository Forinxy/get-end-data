package com.expiryguard.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expiryguard.app.data.db.entity.ProductEntity
import com.expiryguard.app.data.repository.ProductRepository
import com.expiryguard.app.domain.engine.ExpiryRuleEngine
import com.expiryguard.app.domain.model.ProductStatus
import com.expiryguard.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页数据状态
 *
 * @property todayExpiry 今日到期的清单列表（含可退货）
 * @property warning 预警清单列表（还有1天到期）
 * @property completedProducts 已处理的清单列表
 * @property pendingProducts 待处理的清单列表（剩余未完成）
 * @property todayCount 今日到期数量
 * @property warningCount 预警数量
 * @property completedCount 已处理数量
 * @property pendingCount 待处理数量
 * @property recentProducts 最近添加的5个清单
 * @property totalCount 清单总数
 * @property isLoading 是否正在加载
 */
data class HomeUiState(
    val todayExpiry: List<ProductEntity> = emptyList(),
    val warning: List<ProductEntity> = emptyList(),
    val completedProducts: List<ProductEntity> = emptyList(),
    val pendingProducts: List<ProductEntity> = emptyList(),
    val todayCount: Int = 0,
    val warningCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val recentProducts: List<ProductEntity> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

/**
 * 首页 ViewModel，负责加载首页所需的各项数据
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载所有活跃清单数据，并按状态分类统计
     * 归类规则：
     * - 今日到期：到期日期是今天
     * - 可退货：根据规则1，到期前阈值天内放入待办
     * - 预警：还有1天到期
     * - 已处理：isCompleted == true
     * - 待处理：剩下的未完成清单
     */
    private fun loadData() {
        viewModelScope.launch {
            repository.getAllActiveProducts()
                // 过滤、排序、去重等计算在 Default 线程执行，避免阻塞主线程
                .map { products ->
                    val today = DateUtils.todayTimestamp()
                    processProducts(products, today)
                }
                .flowOn(Dispatchers.Default)
                .collect { state ->
                    _uiState.value = state
                    _isRefreshing.value = false
                }
        }
    }

    /**
     * 处理清单列表并更新 UI 状态
     * 在 Default 线程执行，主线程只负责接收结果
     */
    private fun processProducts(products: List<ProductEntity>, today: Long): HomeUiState {
        // 今日到期：到期日期是今天
        val todayExpiry = products.filter {
            DateUtils.isToday(it.expiryDate) && !it.isCompleted
        }

        // 可退货清单：根据规则1，到期前阈值天时放入待办
        val returnable = products.filter { product ->
            val status = ExpiryRuleEngine.calculateStatus(product.shelfLifeDays, product.expiryDate)
            status is ProductStatus.Returnable && !product.isCompleted
        }

        // 已过期：到期日期已过，需要立即处理
        val expired = products.filter {
            val days = DateUtils.daysBetween(today, it.expiryDate)
            days < 0 && !it.isCompleted
        }

        // 今日到期（含可退货、已过期）—— 合并去重
        val todayWithReturnable = (todayExpiry + returnable + expired).distinctBy { it.id }

        // 预警：还有1天到期（排除已在今日待办中的清单）
        val todayWithReturnableIds = todayWithReturnable.map { it.id }.toSet()
        val warning = products.filter {
            val days = DateUtils.daysBetween(today, it.expiryDate)
            days == 1 && !it.isCompleted && it.id !in todayWithReturnableIds
        }

        // 已处理：仅保留今日到期、预警、已过期或可退货的已完成清单
        val completed = products.filter { product ->
            if (!product.isCompleted) return@filter false
            val days = DateUtils.daysBetween(today, product.expiryDate)
            if (days <= 1) return@filter true
            val status = ExpiryRuleEngine.calculateStatus(product.shelfLifeDays, product.expiryDate)
            status is ProductStatus.Returnable
        }

        // 待处理：剩下的未完成清单（排除今日到期、可退货、预警）
        val todayAndWarningIds = (todayWithReturnable.map { it.id } + warning.map { it.id }).toSet()
        val pending = products.filter {
            !it.isCompleted && it.id !in todayAndWarningIds
        }

        // 最近添加：按创建时间降序取前5个
        val recent = products.sortedByDescending { it.createdAt }.take(5)

        return HomeUiState(
            todayExpiry = todayWithReturnable,
            warning = warning,
            completedProducts = completed,
            pendingProducts = pending,
            todayCount = todayWithReturnable.size,
            warningCount = warning.size,
            completedCount = completed.size,
            pendingCount = pending.size,
            recentProducts = recent,
            totalCount = products.size,
            isLoading = false
        )
    }

    /**
     * 手动刷新首页数据
     * 触发下拉刷新指示器显示，并通过 Room Flow 自动响应数据变化
     */
    fun refresh() {
        _isRefreshing.value = true
    }

    /**
     * 切换清单的完成状态
     */
    fun toggleProductCompletion(productId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(productId, isCompleted)
        }
    }
}