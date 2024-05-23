package com.volsib.drawercompose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// типа дата контекст. 1 экзепляр бд
@Database(entities = [Picture::class], version = 1, exportSchema = false)
abstract class DrawerDatabase: RoomDatabase() {

    abstract fun pictureDao(): PictureDao

    companion object {
        @Volatile
        private var Instance: DrawerDatabase? = null

        fun getDatabase(context: Context): DrawerDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DrawerDatabase::class.java, "drawer_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}