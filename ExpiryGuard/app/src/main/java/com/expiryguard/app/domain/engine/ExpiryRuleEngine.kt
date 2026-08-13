package com.expiryguard.app.domain.engine

import com.expiryguard.app.domain.model.ProductStatus
import com.expiryguard.app.util.DateUtils

/**
 * 到期规则引擎，根据产品保质期和到期日期计算状态
 *
 * 规则很简单：分组是分类，阈值决定到期前多少天显示"可退货"
 */
object ExpiryRuleEngine {

    /**
     * 计算产品当前状态（使用硬编码默认阈值）
     *
     * @param shelfLifeDays 保质期天数（用于计算默认阈值）
     * @param expiryDate 到期日期时间戳（毫秒）
     * @return 对应的 ProductStatus
     */
    fun calculateStatus(shelfLifeDays: Int, expiryDate: Long): ProductStatus {
        return calculateStatus(shelfLifeDays, expiryDate, null)
    }

    /**
     * 计算产品当前状态（支持自定义退货阈值）
     *
     * @param shelfLifeDays 保质期天数（用于计算默认阈值）
     * @param expiryDate 到期日期时间戳（毫秒）
     * @param returnThreshold 自定义退货阈值天数，为 null 时使用硬编码默认值
     * @return 对应的 ProductStatus
     */
    fun calculateStatus(
        shelfLifeDays: Int,
        expiryDate: Long,
        returnThreshold: Int?
    ): ProductStatus {
        val now = DateUtils.todayTimestamp()
        val remainingDays = DateUtils.daysBetween(now, expiryDate)

        // 已过期
        if (remainingDays <= 0) {
            return ProductStatus.Expired(daysOverdue = -remainingDays)
        }

        // 紧急状态：剩余天数 <= 3
        if (remainingDays <= 3) {
            return ProductStatus.Urgent(remainingDays = remainingDays)
        }

        val effectiveThreshold = returnThreshold ?: getReturnThreshold(shelfLifeDays)

        // 可退货状态：剩余天数 <= 退货阈值
        // 例如：阈值15天，则到期前15天内为"可退货"
        if (effectiveThreshold > 0 && remainingDays <= effectiveThreshold) {
            return ProductStatus.Returnable(
                remainingDays = remainingDays,
                threshold = effectiveThreshold
            )
        }

        // 安全状态
        return ProductStatus.Safe(remainingDays = remainingDays)
    }

    /**
     * 根据保质期天数获取默认退货阈值天数
     *
     * 规则1：
     * - 保质期 >1年（365天）：剩余0-45天可退货
     * - 保质期 6个月~1年（183~365天）：剩余0-20天可退货
     * - 保质期 3个月~6个月（90~183天）：剩余0-15天可退货
     * - 保质期 <3个月（<90天）：不可退货
     *
     * @param shelfLifeDays 保质期天数
     * @return 退货阈值天数
     */
    fun getReturnThreshold(shelfLifeDays: Int): Int {
        return when {
            shelfLifeDays >= 365 -> 45   // 1 年以上
            shelfLifeDays > 183 -> 20    // 6 个月 ~ 1 年（>183，因为183是3~6个月上限）
            shelfLifeDays >= 90 -> 15    // 3 个月 ~ 6 个月（90~183）
            else -> 0                    // 低于 3 个月，不可退货
        }
    }
}