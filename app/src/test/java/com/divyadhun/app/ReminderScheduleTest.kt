package com.divyadhun.app

import com.divyadhun.app.engagement.DailyReminderSchedule
import com.divyadhun.app.domain.model.ListeningTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduleTest {
    @Test fun listeningTimesMapToStableHours() {
        assertEquals(7, DailyReminderSchedule.hourFor(ListeningTime.MORNING))
        assertEquals(18, DailyReminderSchedule.hourFor(ListeningTime.EVENING))
        assertEquals(21, DailyReminderSchedule.hourFor(ListeningTime.BEFORE_SLEEP))
        assertEquals(8, DailyReminderSchedule.hourFor(ListeningTime.ANYTIME))
    }

    @Test fun nextTriggerMovesToTomorrowAfterTheDailySlot() {
        val now = ZonedDateTime.of(2026, 9, 14, 9, 30, 0, 0, ZoneId.of("UTC"))
        val trigger = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(DailyReminderSchedule.nextTriggerMillis(now, ListeningTime.MORNING)), ZoneId.of("UTC"))
        assertEquals(15, trigger.dayOfMonth)
        assertEquals(7, trigger.hour)
        assertTrue(trigger.isAfter(now))
    }
}
