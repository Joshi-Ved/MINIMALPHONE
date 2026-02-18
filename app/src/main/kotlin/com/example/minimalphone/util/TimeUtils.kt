package com.example.minimalphone.util

import java.time.LocalDate
import java.time.ZoneId

fun todayStamp(zoneId: ZoneId = ZoneId.systemDefault()): String =
    LocalDate.now(zoneId).toString()

fun startOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    LocalDate.now(zoneId)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

fun nextMidnightMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    LocalDate.now(zoneId)
        .plusDays(1)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
