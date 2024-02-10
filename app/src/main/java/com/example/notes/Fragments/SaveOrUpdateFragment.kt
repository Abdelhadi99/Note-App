package com.example.notes.Fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.notes.Activites.MainActivity
import com.example.notes.Model.Note
import com.example.notes.R
import com.example.notes.databinding.BottomSheetLayoutBinding
import com.example.notes.databinding.FragmentNoteBinding
import com.example.notes.databinding.FragmentSaveOrUpdateBinding
import com.example.notes.utils.hideKeyboard
import com.example.notes.viewModel.NoteActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SaveOrUpdateFragment : Fragment(R.layout.fragment_save_or_update) {

    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentSaveOrUpdateBinding
    private var note: Note?= null
    private var color = -1
    private lateinit var result: String
    private val noteActivityViewModel:NoteActivityViewModel by activityViewModels()
    private val currentDate = formatDateTime()
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: SaveOrUpdateFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L

        }

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentBinding = FragmentSaveOrUpdateBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        ViewCompat.setTransitionName(
            contentBinding.fragmentSaveOrUpdateLayout,
            "recyclerView_${args.note?.id}"
        )

        contentBinding.btnBack.setOnClickListener{
            requireView().hideKeyboard()
            navController.popBackStack()
        }



        contentBinding.btnSave.setOnClickListener{
            saveNote()

        }

        try {
            contentBinding.edtNoteContent.setOnFocusChangeListener{_, hasFocus ->
                if (hasFocus){
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.edtNoteContent.setStylesBar(contentBinding.styleBar)
                }else{
                    contentBinding.bottomBar.visibility = View.GONE
                }
            }
        }catch (e: Throwable){
            Log.d("content Edit Text", "Error: ${e.message}")
        }

        contentBinding.fabColorPick.setOnClickListener{
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )

            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null)


            // TODO to show the ColorPicker
            with(bottomSheetDialog){
                setContentView(bottomSheetView)
                show()
            }


            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener {
                        value ->
                        color = value
                        contentBinding.apply {
                            fragmentSaveOrUpdateLayout.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post{
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        // opens with existing note item
        setUpNote()



    }

    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.edtTitle
        val content  = contentBinding.edtNoteContent
        val lastEdited = contentBinding.txtLastEdited

        if (note == null){
            contentBinding.txtLastEdited.text= currentDate
        }


        if (note != null){
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on,note.date)
            color = note.color
            contentBinding.apply {

                job.launch {
                    delay(10)
                    fragmentSaveOrUpdateLayout.setBackgroundColor(color)
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)

            }
            activity?.window?.statusBarColor = note.color
        }


    }

    private fun saveNote() {
        if (! contentBinding.edtTitle.text.toString().isEmpty()){

           if (! contentBinding.edtNoteContent.text.toString().isEmpty()) {

               note = args.note
               when (note) {
                   null ->{
                       noteActivityViewModel.saveNote(
                       Note(
                           0,
                           contentBinding.edtTitle.text.toString(),
                           contentBinding.edtNoteContent.getMD(),
                           currentDate,
                           color
                       )
                   )
                       result = "Note Saved"
                       setFragmentResult(
                           "key",
                           bundleOf("bundleKey" to result)
                       )
                       navController.navigate(SaveOrUpdateFragmentDirections.actionSaveOrUpdateFragmentToNoteFragment())

                   }

                   else -> {
                       //update note
                       updateNote()
                       navController.popBackStack()
                   }

               }

           }else{
               Toast.makeText(activity,R.string.empty_content,Toast.LENGTH_LONG).show()
           }

        }else{
            Toast.makeText(activity,R.string.empty_title,Toast.LENGTH_LONG).show()
        }
    }

    private fun updateNote() {
        if (note != null){
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.edtTitle.text.toString(),
                    contentBinding.edtNoteContent.text.toString(),
                    currentDate,
                    color
                )
            )
        }
    }

    fun formatDateTime(): String {
        val currentLocale = Locale.getDefault()
        val formatPattern = if (currentLocale.language == "ar") {
            "d MMM, yyyy | h:mm a"
        } else {
            "MMM d, yyyy | h:mm a"
        }

        val simpleDateFormat = SimpleDateFormat(formatPattern, currentLocale)
        return simpleDateFormat.format(Date())
    }

}