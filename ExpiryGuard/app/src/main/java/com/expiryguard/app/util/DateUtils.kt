package com.expiryguard.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日期工具类，提供常用日期格式化和计算功能。
 */
object DateUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd")

    /**
     * 格式化为完整日期字符串（yyyy-MM-dd）
     */
    fun formatDate(timestamp: Long): String {
        return toLocalDate(timestamp).format(dateFormatter)
    }

    /**
     * 格式化为短日期字符串（MM/dd）
     */
    fun formatShortDate(timestamp: Long): String {
        return toLocalDate(timestamp).format(shortDateFormatter)
    }

    /**
     * 计算两个时间戳之间的天数差
     */
    fun daysBetween(from: Long, to: Long): Int {
        val fromDate = toLocalDate(from)
        val toDate = toLocalDate(to)
        return (toDate.toEpochDay() - fromDate.toEpochDay()).toInt()
    }

    /**
     * 将时间戳转为 LocalDate
     */
    fun toLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    /**
     * 将 LocalDate 转为当天开始时间的时间戳
     */
    fun toTimestamp(localDate: LocalDate): Long {
        return localDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * 获取今天开始时间的时间戳
     */
    fun todayTimestamp(): Long {
        return toTimestamp(LocalDate.now())
    }

    /**
     * 判断时间戳是否属于今天
     */
    fun isToday(timestamp: Long): Boolean {
        return toLocalDate(timestamp) == LocalDate.now()
    }

    /**
     * 判断时间戳是否属于明天
     */
    fun isTomorrow(timestamp: Long): Boolean {
        return toLocalDate(timestamp) == LocalDate.now().plusDays(1)
    }

    /**
     * 在指定时间戳上增加天数，返回新的时间戳
     */
    fun addDays(timestamp: Long, days: Int): Long {
        val date = toLocalDate(timestamp)
        val newDate = date.plusDays(days.toLong())
        return toTimestamp(newDate)
    }
}