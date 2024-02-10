package com.example.notes.repository

import com.example.notes.DB.NoteDatabase
import com.example.notes.Model.Note

class NoteRepository(private val dp:NoteDatabase) {

    fun getNote() = dp.getNoteDao().getAllData()

    fun searchNote(query:String) = dp.getNoteDao().searchNote(query)

    suspend fun addNote(note: Note) = dp.getNoteDao().addNote(note)

    suspend fun updateNote(note: Note) = dp.getNoteDao().updateNote(note)

    suspend fun deleteNote(note: Note) = dp.getNoteDao().deleteNote(note)

}