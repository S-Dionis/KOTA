package com.mwk.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mwk.data.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Query(
        """
        select * from entries
        where endTimeMillis >= :startTimeMillis
        and startTimeMillis <= :endTimeMillis
        order by startTimeMillis asc
        """
    )
    fun observeEntries(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): Flow<List<EntryEntity>>

    @Query("select * from entries where id = :id limit 1")
    fun selectEntry(id: Long): Flow<EntryEntity?>

    @Query("update entries set isEnabled = :isEnabled where id = :entryId")
    suspend fun updateEntryEnabled(entryId: Long, isEnabled: Boolean)

}
