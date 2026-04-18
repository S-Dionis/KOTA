package com.mwk.kota

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getLocalDate(timeMillis: Long, zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(timeMillis)
            .atZone(zoneId)
            .toLocalDate()
    }

    fun getStartOfDayMillis(localDate: LocalDate, zoneId: ZoneId): Long {
        return localDate
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun getStartOfDayMillis(millis: Long, zoneId: ZoneId): Long {
        return getStartOfDayMillis(
            Instant.ofEpochMilli(millis)
                .atZone(zoneId)
                .toLocalDate(), zoneId
        )

    }

    fun getEndOfDayMillis(localDate: LocalDate, zoneId: ZoneId): Long {
        return getStartOfDayMillis(localDate.plusDays(1), zoneId) - 1
    }

    fun formatTimeMillis(millis: Long, zoneId: ZoneId): String {
        return Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun formatDateTime24HourMillis(
        millis: Long,
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)
        return formatter.format(Date(millis))
    }

    fun formatDateTime12HourMillis(
        millis: Long,
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy hh:mm a", locale)
        return formatter.format(Date(millis))
    }

    fun formatDateMillis(
        millis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String {
        return Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))
    }
}
