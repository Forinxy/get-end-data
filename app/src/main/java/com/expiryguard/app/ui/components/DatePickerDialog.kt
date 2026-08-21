package com.expiryguard.app.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 日期选择对话框，包装 Material3 DatePickerDialog。
 *
 * 只选几月几日，年份固定为今年（默认自动填充当年）。
 *
 * @param initialDate 初始选中日期，默认为今天
 * @param onDateSelected 选中日期回调
 * @param onDismiss 关闭对话框回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val now = LocalDate.now()

    // 年份固定为今年，仅保留初始日期的月/日（闰年 2/29 在当前非闰年时回退到今天）
    val pickerDate = try {
        LocalDate.of(now.year, initialDate.monthValue, initialDate.dayOfMonth)
    } catch (_: Exception) {
        now
    }

    // 将 LocalDate 转为毫秒时间戳
    val initialMillis = pickerDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = now.year..now.year
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}