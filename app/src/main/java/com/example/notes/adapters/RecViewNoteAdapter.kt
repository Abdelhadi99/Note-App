package com.example.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.Fragments.NoteFragmentDirections
import com.example.notes.Model.Note
import com.example.notes.R
import com.example.notes.databinding.ItemNoteBinding
import com.example.notes.utils.hideKeyboard
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class RecViewNoteAdapter : ListAdapter<Note,RecViewNoteAdapter.NotesViewHolder>(DiffUtilCallback()) {

    inner class NotesViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

        private val contentBinding = ItemNoteBinding.bind(itemView)
        val title: MaterialTextView = contentBinding.txtNoteTitle
        val content: TextView = contentBinding.txtNoteContent
        val date : TextView = contentBinding.txtNoteDate
        val parent : MaterialCardView = contentBinding.noteItemCardView
        val markWon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin(){
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ){visitor,_ -> visitor.forceNewLine()}
                }
            })
            .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note,parent,false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        getItem(position).let { note ->
            holder.apply {

                parent.transitionName = "recyclerView_${note.id}"
                title.text = note.title
                markWon.setMarkdown(content,note.content)
                date.text = note.date
                parent.setCardBackgroundColor(note.color)

                title.setOnClickListener{

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)

                }

                date.setOnClickListener{

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)

                }

                itemView.setOnClickListener{

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)

                }
                content.setOnClickListener{


                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)


                }

            }
        }
    }

}