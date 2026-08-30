package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [KivyProjectEntity::class], version = 1, exportSchema = false)
abstract class KivyDatabase : RoomDatabase() {
    abstract fun kivyProjectDao(): KivyProjectDao

    companion object {
        @Volatile
        private var INSTANCE: KivyDatabase? = null

        fun getInstance(context: Context): KivyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KivyDatabase::class.java,
                    "kivy_python_studio.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default projects
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).kivyProjectDao().insertAll(DefaultProjects.list)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
