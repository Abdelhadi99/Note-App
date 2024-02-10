package com.example.notes.Activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.notes.DB.NoteDatabase
import com.example.notes.Model.Note
import com.example.notes.R
import com.example.notes.databinding.ActivityMainBinding
import com.example.notes.repository.NoteRepository
import com.example.notes.viewModel.NoteActivityViewModel
import com.example.notes.viewModel.NoteActivityViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)


        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this,
            noteActivityViewModelFactory)[NoteActivityViewModel::class.java]

        }   catch (e: java.lang.Exception){
            Log.d("Main Tag","Error: ${e.message}")
        }





    }
}




