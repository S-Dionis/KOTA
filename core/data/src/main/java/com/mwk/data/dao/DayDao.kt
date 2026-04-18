package com.mwk.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.mwk.data.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {

    @Query(
        """
        select * from entries
        where startTimeMillis < :endOfDay
          and endTimeMillis > :startOfDay
        order by startTimeMillis asc
    """)
    fun observeTodayEntries(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<EntryEntity>>

}