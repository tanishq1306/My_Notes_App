package com.example.mynotesapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mynotesapp.R
import com.example.mynotesapp.database.Note
import com.example.mynotesapp.database.NotesDatabase
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.note_item_layout.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateNote : BaseFragment() {
    private var currentDate: String = ""
    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("noteId",-1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateNote().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Edit Note
        if (noteId != -1) {
            delete_note.visibility = View.VISIBLE
            launch {
                context?.let {
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    etNoteTitle.setText(notes.title)
                    etNoteDesc.setText(notes.noteText)

                    // url
                    if (!notes.webLink.isNullOrBlank()) {
                        imp_links.visibility = View.VISIBLE
                        imp_links.setText(notes.webLink)
                        var url: String = notes.webLink!!
                        imp_links.setOnClickListener {
                            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    }
                }
            }
        }
        else {
            delete_note.visibility = View.GONE
        }

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currentDate = sdf.format(Date());
        tvDateTime.text = currentDate

        saveNote.setOnClickListener {
            if (noteId != -1) {
                updateNote()
            }
            else {
                saveNote()
            }
        }

        back.setOnClickListener {
            replaceFragment(HomeFragment.newInstance(), false)
        }

        add_image_path.setOnClickListener {
            // pick a image
            Toast.makeText(context, "Add Image", Toast.LENGTH_SHORT).show()
        }

        add_link.setOnClickListener{
            // add a link
            imp_links.visibility = View.VISIBLE
            Toast.makeText(context, "Add Web Link", Toast.LENGTH_SHORT).show()
        }

        delete_note.setOnClickListener {
            // delete note
            Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
            deleteNote()
        }
    }

    private fun saveNote() {
        when {
            etNoteTitle.text.isNullOrEmpty() -> {
                Toast.makeText(context,"Note Title is Required",Toast.LENGTH_SHORT).show()
            }
            etNoteDesc.text.isNullOrEmpty() -> {
                Toast.makeText(context,"Note Description is Required",Toast.LENGTH_SHORT).show()
            }
            else -> {
                launch {
                    val notes = Note()
                    notes.title = etNoteTitle.text.toString()
                    notes.dateTime = currentDate
                    notes.noteText = etNoteDesc.text.toString()

                    if (imp_links.text.toString().isNotEmpty()) {
                        notes.webLink = imp_links.text.toString()
                    }

                    context ?.let {
                        NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                        Toast.makeText(context, "${notes.title} Saved Successfully !!", Toast.LENGTH_SHORT).show()
                        etNoteTitle.setText("")
                        etNoteDesc.setText("")
                        if (imp_links.visibility == View.VISIBLE) imp_links.visibility = View.GONE
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun updateNote() {
        launch {
            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                notes.title = etNoteTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate

                if (imp_links.text.toString().isNotEmpty()) {
                    notes.webLink = imp_links.text.toString()
                }

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                etNoteTitle.setText("")
                etNoteDesc.setText("")
                if (imp_links.visibility == View.VISIBLE) imp_links.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun deleteNote(){
        launch {
            context?.let {
                NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, isTransition:Boolean) {
        val fragmentTransition = activity!!.supportFragmentManager.beginTransaction()

        if (isTransition){
            fragmentTransition.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
        }

        fragmentTransition.add(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().supportFragmentManager.popBackStack()
        replaceFragment(HomeFragment.newInstance(), false)
    }
}