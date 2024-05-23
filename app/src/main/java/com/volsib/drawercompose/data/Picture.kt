package com.volsib.drawercompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pictures")
data class Picture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    var image: ByteArray?
)
