package com.expiryguard.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.expiryguard.app.data.db.entity.ProductEntity
import com.expiryguard.app.domain.engine.ExpiryRuleEngine
import com.expiryguard.app.domain.model.ProductStatus
import com.expiryguard.app.ui.components.EmptyState
import com.expiryguard.app.ui.components.GlassCard
import com.expiryguard.app.ui.components.GradientBackground
import com.expiryguard.app.ui.components.StatusBadge
import com.expiryguard.app.ui.theme.Blue500
import com.expiryguard.app.ui.theme.Gray500
import com.expiryguard.app.ui.theme.Green500
import com.expiryguard.app.ui.theme.Orange500
import com.expiryguard.app.ui.theme.Red500
import com.expiryguard.app.ui.theme.Red700
import com.expiryguard.app.ui.theme.Yellow500
import com.expiryguard.app.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 获取产品状态对应的颜色
 */
private fun statusToColor(status: ProductStatus): Color {
    return when (status) {
        is ProductStatus.Safe -> Green500
        is ProductStatus.ExpiringSoon -> Yellow500
        is ProductStatus.Returnable -> Blue500
        is ProductStatus.Urgent -> Red500
        is ProductStatus.Expired -> Red700
    }
}

/**
 * 根据 ProductEntity 计算状态颜色
 */
private fun productStatusColor(product: ProductEntity): Color {
    val status = ExpiryRuleEngine.calculateStatus(product.shelfLifeDays, product.expiryDate)
    return statusToColor(status)
}

/**
 * 首页可组合函数
 *
 * 简洁版：仅展示日期问候语、搜索栏和今日待办任务列表。
 * 点击任务卡片通过底部弹出层查看详情，不再跳转独立页面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToProductDetail: (Long) -> Unit = {},
    onNavigateToProductList: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // 底部弹出层状态
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // 选中清单时展开底部弹出层
    LaunchedEffect(selectedProduct) {
        if (selectedProduct != null) {
            sheetState.expand()
        }
    }

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing.collectAsState().value,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.totalCount == 0 && !uiState.isLoading) {
            GradientBackground {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = "还没有清单，点击下方按钮添加第一个清单",
                        icon = Icons.Outlined.AddCircleOutline,
                        actionText = "添加清单",
                        onAction = onNavigateToAddProduct
                    )
                }
            }
        } else {
            GradientBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 顶部：日期 + 问候语
                    GreetingSection()

                    Spacer(modifier = Modifier.height(16.dp))

                    // 搜索栏
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            onNavigateToProductList(searchQuery, "ALL")
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 今日待办任务列表 —— 今天到期 + 还剩1天的预警清单
                    TodayTasksSection(
                        todayExpiry = uiState.todayExpiry,
                        warning = uiState.warning,
                        completedProducts = uiState.completedProducts,
                        onTaskCompleted = { productId, isCompleted ->
                            viewModel.toggleProductCompletion(productId, isCompleted)
                        },
                        onTaskClick = { product ->
                            selectedProduct = product
                        }
                    )
                }
            }
        }
    }

    // 底部弹出层：清单详情
    if (selectedProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ProductDetailSheet(
                product = selectedProduct!!,
                onMarkComplete = {
                    viewModel.toggleProductCompletion(
                        selectedProduct!!.id,
                        !selectedProduct!!.isCompleted
                    )
                    selectedProduct = null
                },
                onNavigateToDetail = {
                    onNavigateToProductDetail(selectedProduct!!.id)
                    selectedProduct = null
                },
                onDismiss = { selectedProduct = null }
            )
        }
    }
}

/**
 * 顶部问候语区域
 */
@Composable
private fun GreetingSection() {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    val dateString = today.format(formatter)

    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..5 -> "夜深了"
        in 6..11 -> "早上好"
        in 12..13 -> "中午好"
        in 14..17 -> "下午好"
        else -> "晚上好"
    }

    Column {
        // 装饰性顶部渐变条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Blue500.copy(alpha = 0.6f),
                            Green500.copy(alpha = 0.6f),
                            Yellow500.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 装饰性圆形指示器
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Blue500.copy(alpha = 0.15f),
                                Green500.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = Blue500.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Green500.copy(alpha = 0.8f))
                )
            }
        }
    }
}

/**
 * 搜索栏组件
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    GlassCard(
        onClick = onSearch,
        shape = RoundedCornerShape(14.dp),
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Blue500.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Blue500.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (query.isNotBlank()) query else "搜索清单名称或备注",
                style = MaterialTheme.typography.bodyLarge,
                color = if (query.isNotBlank())
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * 今日待办区域 —— 显示今天到期 + 还剩1天的预警清单
 *
 * 支持左右滑动切换完成状态（左右方向均可标记/取消标记）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayTasksSection(
    todayExpiry: List<ProductEntity>,
    warning: List<ProductEntity>,
    completedProducts: List<ProductEntity>,
    onTaskCompleted: (Long, Boolean) -> Unit,
    onTaskClick: (ProductEntity) -> Unit
) {
    // 合并今日到期 + 预警（还剩1天），按到期日期排序
    val todayTasks = (todayExpiry + warning)
        .distinctBy { it.id }
        .sortedBy { it.expiryDate }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日待办",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (todayTasks.isNotEmpty()) {
                val unfinishedCount = todayTasks.count { !it.isCompleted }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (unfinishedCount > 0) Red500.copy(alpha = 0.12f) else Green500.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$unfinishedCount 项未处理",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unfinishedCount > 0) Red500 else Green500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (todayTasks.isEmpty() && completedProducts.isEmpty()) {
            GlassCard(
                shape = RoundedCornerShape(14.dp),
                elevation = 2.dp
            ) {
                Text(
                    text = "今天没有到期清单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 今天到期的未处理清单 + 预警
                val unfinished = todayTasks.filter { !it.isCompleted }
                if (unfinished.isNotEmpty()) {
                    unfinished.forEach { product ->
                        key(product.id) {
                            TaskCard(
                                product = product,
                                isCompleted = false,
                                onSwipeComplete = {
                                    onTaskCompleted(product.id, true)
                                },
                                onClick = { onTaskClick(product) }
                            )
                        }
                    }
                }

                // 已处理（今天处理过的记录）
                if (completedProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "已处理",
                        style = MaterialTheme.typography.titleSmall,
                        color = Green500,
                        fontWeight = FontWeight.Medium
                    )
                    completedProducts.sortedByDescending { it.expiryDate }.forEach { product ->
                        key(product.id) {
                            TaskCard(
                                product = product,
                                isCompleted = true,
                                onSwipeComplete = {
                                    onTaskCompleted(product.id, false)
                                },
                                onClick = { onTaskClick(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 待办任务卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    product: ProductEntity,
    isCompleted: Boolean,
    onSwipeComplete: () -> Unit,
    onClick: () -> Unit
) {
    val productStatus = remember { ExpiryRuleEngine.calculateStatus(product.shelfLifeDays, product.expiryDate) }
    // 已处理时用绿色压过原始状态色
    val statusColor = if (isCompleted) Green500 else statusToColor(productStatus)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // 不管左滑还是右滑，都触发标记完成/取消完成
            // 返回 false 防止卡片完全滑出（只触发动作，卡片回弹原位）
            onSwipeComplete()
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green500.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = if (isCompleted) "取消完成" else "标记完成",
                    tint = Green500,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        content = {
            GlassCard(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                statusColor = statusColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 左侧状态色条 — 4dp 宽，与卡片等高
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                            .background(statusColor.copy(alpha = 0.8f))
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 清单图片
                            AsyncImage(
                                model = product.photoPath,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // 清单信息
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isCompleted)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val today = DateUtils.todayTimestamp()
                                val daysLeft = DateUtils.daysBetween(today, product.expiryDate)
                                val daysText = when {
                                    productStatus is ProductStatus.Returnable -> {
                                        val r = productStatus as ProductStatus.Returnable
                                        val returnDeadlineDays = (r.remainingDays - r.threshold).coerceAtLeast(0)
                                        if (returnDeadlineDays > 0) {
                                            "可退货 · 剩余退货${returnDeadlineDays}天 · 到期还有${r.remainingDays}天"
                                        } else {
                                            "可退货 · 到期还有${r.remainingDays}天"
                                        }
                                    }
                                    daysLeft < 0 -> "已过期 ${-daysLeft}天"
                                    daysLeft == 0 -> "今天到期"
                                    daysLeft == 1 -> "明天到期（预警）"
                                    else -> "剩余 ${daysLeft}天"
                                }
                                val daysColor = when {
                                    productStatus is ProductStatus.Returnable -> Blue500
                                    daysLeft < 0 -> Red500
                                    daysLeft <= 1 -> Orange500
                                    daysLeft <= 3 -> Yellow500
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }

                                Text(
                                    text = "到期: ${DateUtils.formatDate(product.expiryDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = daysText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = daysColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // 状态标签：已处理时显示绿色"已处理"，否则显示原始状态
                            if (isCompleted) {
                                StatusBadgeCompleted()
                            } else {
                                StatusBadge(status = productStatus)
                            }
                        }

                        // 完成标记
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "已完成",
                                tint = Green500,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

/**
 * 底部弹出层：清单详情
 *
 * 展示清单完整信息，支持标记完成/取消完成、查看完整详情、删除操作。
 * 不跳转新页面，在当前页面以弹出层形式呈现。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailSheet(
    product: ProductEntity,
    onMarkComplete: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    val status = remember { ExpiryRuleEngine.calculateStatus(product.shelfLifeDays, product.expiryDate) }
    val today = remember { DateUtils.todayTimestamp() }
    val daysLeft = remember { DateUtils.daysBetween(today, product.expiryDate) }

    val daysText = when {
        daysLeft < 0 -> "已过期 ${-daysLeft}天"
        daysLeft == 0 -> "今天到期"
        daysLeft == 1 -> "明天到期（预警）"
        else -> "剩余 ${daysLeft}天"
    }
    val daysColor = when {
        daysLeft < 0 -> Red500
        daysLeft <= 1 -> Orange500
        daysLeft <= 3 -> Yellow500
        else -> Green500
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // 拖拽指示条
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 清单图片（如果有）
        if (product.photoPath != null) {
            AsyncImage(
                model = product.photoPath,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 清单名称 + 状态标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 到期日期
        DetailInfoRow(
            label = "到期日期",
            value = DateUtils.formatDate(product.expiryDate)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 剩余天数
        DetailInfoRow(
            label = "状态",
            value = daysText,
            valueColor = daysColor
        )

        // 可退货信息
        if (status is ProductStatus.Returnable) {
            Spacer(modifier = Modifier.height(10.dp))
            val returnDeadlineDays = (status.remainingDays - status.threshold).coerceAtLeast(0)
            if (returnDeadlineDays > 0) {
                DetailInfoRow(
                    label = "剩余退货",
                    value = "${returnDeadlineDays}天",
                    valueColor = Blue500
                )
            } else {
                DetailInfoRow(
                    label = "可退货",
                    value = "退货期内，到期还有${status.remainingDays}天",
                    valueColor = Blue500
                )
            }
        }

        // 生产日期
        if (product.productionDate != null) {
            Spacer(modifier = Modifier.height(10.dp))
            DetailInfoRow(
                label = "生产日期",
                value = DateUtils.formatDate(product.productionDate!!)
            )
        }

        // 保质期
        if (product.shelfLifeDays > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            DetailInfoRow(
                label = "保质期",
                value = "${product.shelfLifeDays}天"
            )
        }

        // 备注
        if (!product.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "备注",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = product.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 操作按钮
        // 标记完成/取消完成
        Button(
            onClick = onMarkComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (product.isCompleted) Gray500 else Green500
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (product.isCompleted) "取消完成" else "标记已处理",
                style = MaterialTheme.typography.titleSmall
            )
        }

        }
}

/**
 * 详情信息行
 */
@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (valueColor != MaterialTheme.colorScheme.onSurface) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * 已处理状态标签 — 绿色"已处理"压过原始过期/紧急标记
 */
@Composable
private fun StatusBadgeCompleted() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Green500.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = Green500.copy(alpha = 0.45f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        // 左侧色条
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Green500.copy(alpha = 0.85f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        // 发光圆点
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Green500.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Green500.copy(alpha = 0.9f))
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "已处理",
            color = Green500.copy(alpha = 0.95f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}