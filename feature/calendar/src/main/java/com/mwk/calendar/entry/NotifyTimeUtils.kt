package com.mwk.calendar.entry

object NotifyTimeUtils {

    const val ONE_HOUR_MILLIS: Long = 60 * 60 * 1000L
    const val ONE_DAY_MILLIS: Long = 24 * 60 * 60 * 1000L

    fun getNotifyTimeMillis(
        type: NotifyType,
        startTimeMillis: Long?,
        customDateTimeMillis: Long?,
    ): Long? {
        return when (type) {
            NotifyType.ONE_HOUR -> startTimeMillis?.minus(ONE_HOUR_MILLIS)
            NotifyType.ONE_DAY -> startTimeMillis?.minus(ONE_DAY_MILLIS)
            NotifyType.CUSTOM -> customDateTimeMillis
            NotifyType.NONE -> null
        }
    }

    fun getNotifyType(remindBeforeMillis: Long): NotifyType {
        return when (remindBeforeMillis) {
            ONE_HOUR_MILLIS -> NotifyType.ONE_HOUR
            ONE_DAY_MILLIS -> NotifyType.ONE_DAY
            else -> NotifyType.CUSTOM
        }
    }
}
