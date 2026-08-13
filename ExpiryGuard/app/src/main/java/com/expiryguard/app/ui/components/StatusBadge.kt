package com.expiryguard.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expiryguard.app.domain.model.ProductStatus
import com.expiryguard.app.ui.theme.Blue500
import com.expiryguard.app.ui.theme.Gray500
import com.expiryguard.app.ui.theme.Green500
import com.expiryguard.app.ui.theme.Red500
import com.expiryguard.app.ui.theme.Yellow500

/**
 * 状态标签组件，根据 ProductStatus 显示对应的颜色和文字。
 * 毛玻璃风格设计，带彩色背景和圆角。
 *
 * @param status 产品状态
 */
@Composable
fun StatusBadge(status: ProductStatus) {
    when (status) {
        is ProductStatus.Safe -> {
            // 绿色玻璃标签 + "安全"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = Green500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Green500.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                // 发光圆点
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Green500.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Green500)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "安全",
                    color = Green500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        is ProductStatus.ExpiringSoon -> {
            // 黄色玻璃标签 + "即将到期"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = Yellow500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Yellow500.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Yellow500.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Yellow500)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "即将到期",
                    color = Yellow500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        is ProductStatus.Returnable -> {
            // 蓝色玻璃标签 + "可退货"（天数详情在卡片中展示）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = Blue500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Blue500.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Blue500.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Blue500)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "可退货",
                    color = Blue500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        is ProductStatus.Urgent -> {
            // 红色闪烁玻璃标签 + "紧急 N天"
            val infiniteTransition = rememberInfiniteTransition(label = "urgent_blink")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = Red500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Red500.copy(alpha = alpha * 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Red500.copy(alpha = 0.3f * alpha))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Red500.copy(alpha = alpha))
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "紧急 ${status.remainingDays}天",
                    color = Red500.copy(alpha = alpha),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        is ProductStatus.Expired -> {
            // 灰色玻璃标签 + "已过期 N天"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Gray500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Gray500.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Gray500)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "已过期 ${status.daysOverdue}天",
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}