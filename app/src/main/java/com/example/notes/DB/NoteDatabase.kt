package com.example.notes.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notes.Model.Note


@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false
)

abstract class NoteDatabase: RoomDatabase() {

    abstract fun getNoteDao(): DAO

    companion object{

        // The @Volatile annotation ensures that the instance is always up-to-date and visible to all threads.
        @Volatile
        private var instance: NoteDatabase? = null

        // Defines an object used for synchronization to ensure that the singleton instance is created in a thread-safe manner.
        private val LOCK = Any()

        // invoke method using Singleton pattern get OR create instance of the Database
        operator fun invoke (context: Context) = instance ?: synchronized(LOCK){
            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        // method to create New Room database
        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "note_database"
        ).build()
    }


}