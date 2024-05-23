package com.volsib.drawercompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PictureDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(picture: Picture)

    @Update
    suspend fun update(picture: Picture)

    @Delete
    suspend fun delete(picture: Picture)

    @Query("SELECT * FROM pictures WHERE name = :name")
    suspend fun getPictureByName(name: String): Picture?
}
