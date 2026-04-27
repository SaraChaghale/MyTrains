package com.sachna.mytrains.bdroom.bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sachna.mytrains.bdroom.dao.WorkoutDao
import com.sachna.mytrains.bdroom.entity.WorkoutEntity
import com.sachna.mytrains.bdroom.utils.DateConverters

@Database(entities = [WorkoutEntity::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mygains_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}