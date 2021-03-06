package com.example.mynotesapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotesapp.R
import com.example.mynotesapp.adapter.NotesAdapter
import com.example.mynotesapp.database.Note
import com.example.mynotesapp.database.NotesDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment() {

    var arrNotes = ArrayList<Note>()
    var notesAdapter: NotesAdapter = NotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteRecyclerView.setHasFixedSize(true)
        noteRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        launch {
            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                notesAdapter.setData(notes)
                arrNotes = notes as ArrayList<Note>
                noteRecyclerView.adapter = notesAdapter
            }
        }

        notesAdapter.setOnClickListener(onClicked)
        createNewNote.setOnClickListener {
            replaceFragment(CreateNote.newInstance(), true)
        }

        // inputSearch
        search_view.setOnQueryTextListener( object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                var tempArr = java.util.ArrayList<Note>()

                for (arr in arrNotes){
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString())){
                        tempArr.add(arr)
                    }
                }

                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun replaceFragment(fragment: Fragment, isTransition:Boolean) {
        val fragmentTransition = activity!!.supportFragmentManager.beginTransaction()

        if (isTransition){
            fragmentTransition.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
        }

        fragmentTransition.add(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

    private val onClicked = object :NotesAdapter.OnItemClickListener{
        override fun onClicked(notesId: Int) {

            var fragment :Fragment
            var bundle = Bundle()
            bundle.putInt("noteId", notesId)
            fragment = CreateNote.newInstance()
            fragment.arguments = bundle

            replaceFragment(fragment,false)
        }
    }
}