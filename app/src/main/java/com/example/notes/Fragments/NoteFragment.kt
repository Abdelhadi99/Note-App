package com.example.notes.Fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notes.Activites.MainActivity
import com.example.notes.R
import com.example.notes.adapters.RecViewNoteAdapter
import com.example.notes.databinding.FragmentNoteBinding
import com.example.notes.utils.SwipeToDelete
import com.example.notes.utils.hideKeyboard
import com.example.notes.viewModel.NoteActivityViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter: RecViewNoteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {

        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }

        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }


        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteBinding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)

        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }

        noteBinding.addNoteFab.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())
        }

        noteBinding.inerFab.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())
        }

        recyclerViewDisplay()
        swipeToDelete(noteBinding.recViewNotes)
        // implements search her
        noteBinding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
               noteBinding.imgNoData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    val text = s.toString()
                    val query = "%$text%"


                    if (query.isNotEmpty()){
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner){

                            rvAdapter.submitList(it)

                        }
                    }else{
                        observeDataChanges()
                    }


                }else{
                    observeDataChanges()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        noteBinding.edtSearch.setOnEditorActionListener{v,actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

        noteBinding.recViewNotes.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->

            when{
                scrollY>oldScrollY ->{
                    noteBinding.chatFabText.isVisible = false
                }
                scrollX == scrollY ->{
                    noteBinding.chatFabText.isVisible = true
                }
                else -> {
                    noteBinding.chatFabText.isVisible = true
                }
            }

        }




    }

    private fun swipeToDelete(recViewNotes: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete()
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = rvAdapter.currentList[position]
                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.edtSearch.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (noteBinding.edtSearch.text.toString().isEmpty()){
                    observeDataChanges()
                }
                val snackbar = Snackbar.make(
                    requireView(),getString(R.string.note_deleted),Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction(getString(R.string.undo)){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped = true
                            noteBinding.imgNoData.isVisible = false
                        }

                        super.onShown(transientBottomBar)

                    }

                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab)
                }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackbar.show()
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recViewNotes)



    }

    private fun observeDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner){ list ->
            noteBinding.imgNoData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
       when(resources.configuration.orientation){
           Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
           Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
       }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        noteBinding.recViewNotes.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter = RecViewNoteAdapter()
            rvAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }

        }
        observeDataChanges()


    }

}