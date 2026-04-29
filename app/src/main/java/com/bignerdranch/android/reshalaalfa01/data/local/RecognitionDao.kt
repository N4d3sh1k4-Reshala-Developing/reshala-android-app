package com.bignerdranch.android.reshalaalfa01.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecognitionDao {
    @Query("SELECT * FROM recognition_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<RecognitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecognitionEntity>)

    @Query("DELETE FROM recognition_history")
    suspend fun clearAll()

    @Transaction
    suspend fun updateHistory(items: List<RecognitionEntity>) {
        clearAll()
        insertAll(items)
    }
}
