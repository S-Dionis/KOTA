package com.mwk.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mwk.data.entity.EntryNotificationEntity
import com.mwk.data.model.EntryReminderSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<EntryNotificationEntity>)

    @Query("delete from notifications where entryId = :entryId")
    suspend fun deleteByEntryId(entryId: Long)

    @Query("update notifications set isEnabled = :isEnabled where entryId = :entryId")
    suspend fun updateEntryNotificationsEnabled(entryId: Long, isEnabled: Boolean)

    @Query(
        """
        update notifications
        set lastTriggeredOccurrenceStartMillis = :occurrenceStartMillis
        where id = :notificationId
        """
    )
    suspend fun updateLastTriggeredOccurrence(notificationId: Long, occurrenceStartMillis: Long)

    @Query("select * from notifications where entryId = :entryId order by remindBeforeMillis asc, id asc")
    fun observeNotifications(entryId: Long): Flow<List<EntryNotificationEntity>>

    @Transaction
    suspend fun replaceNotifications(entryId: Long, notifications: List<EntryNotificationEntity>) {
        deleteByEntryId(entryId)
        if (notifications.isNotEmpty()) {
            insertNotifications(notifications)
        }
    }

    @Query(
        """
        select
            n.id as notification_id,
            n.entryId as notification_entryId,
            n.remindBeforeMillis as notification_remindBeforeMillis,
            n.isEnabled as notification_isEnabled,
            n.lastTriggeredOccurrenceStartMillis as notification_lastTriggeredOccurrenceStartMillis,
            e.name as entry_name,
            e.startTimeMillis as entry_startTimeMillis,
            e.imagePath as entry_imagePath
        from notifications n
        inner join entries e on e.id = n.entryId
        where n.isEnabled = 1
        order by e.startTimeMillis asc, n.remindBeforeMillis asc, n.id asc
        """
    )
    fun observeActiveSchedules(): Flow<List<EntryReminderSchedule>>

    @Query(
        """
        select
            n.id as notification_id,
            n.entryId as notification_entryId,
            n.remindBeforeMillis as notification_remindBeforeMillis,
            n.isEnabled as notification_isEnabled,
            n.lastTriggeredOccurrenceStartMillis as notification_lastTriggeredOccurrenceStartMillis,
            e.name as entry_name,
            e.startTimeMillis as entry_startTimeMillis,
            e.imagePath as entry_imagePath
        from notifications n
        inner join entries e on e.id = n.entryId
        where n.id = :notificationId
        limit 1
        """
    )
    suspend fun getSchedule(notificationId: Long): EntryReminderSchedule?
}
