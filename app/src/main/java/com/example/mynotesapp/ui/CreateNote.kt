package com.example.mynotesapp.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class CreateNote : BaseFragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    private var currentDate: String = ""
    private var noteId = -1
    private var readPermissionStorage = 1
    private var requestCodeImage = 2
    private var selectedImagePath = ""

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
                context?.let { it ->
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    etNoteTitle.setText(notes.title)
                    etNoteDesc.setText(notes.noteText)

                    // url
                    if (!notes.webLink.isNullOrBlank()) {
                        imp_links.visibility = View.VISIBLE
                        imp_links.setText(notes.webLink)
                        var url: String = notes.webLink!!
                        imp_links.setOnClickListener {
                            val builder: AlertDialog.Builder? = activity?.let {
                                AlertDialog.Builder(it)
                            }

                            builder?.setMessage("Go to $url")?.
                                setPositiveButton("Yes",
                                    DialogInterface.OnClickListener { _, _ ->
                                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    startActivity(intent)
                                })?.setNegativeButton("No",
                                    DialogInterface.OnClickListener { dialog, _ ->
                                        // do nothing
                                        imp_links.isClickable = false
                                        dialog.cancel()
                                })

                            builder?.create()?.show()
                        }
                    }

                    // image
                    if (!notes.imgPath.isNullOrBlank()) {
                        selectedImagePath = notes.imgPath!!
                        imageNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
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
            readStorageTask()
        }

        add_link.setOnClickListener{
            // add a link
            imp_links.visibility = View.VISIBLE
            Toast.makeText(context, "Add Web Link", Toast.LENGTH_SHORT).show()
        }

        delete_note.setOnClickListener {
            // delete note
            deleteNote()
            Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
        }

        imageDelete.setOnClickListener{
            Toast.makeText(context, "Image Deleted", Toast.LENGTH_SHORT).show()
            selectedImagePath = ""
            layoutImage.visibility = View.GONE
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
                    if (!selectedImagePath.isNullOrBlank() && selectedImagePath.isNotEmpty()) {
                        notes.imgPath = selectedImagePath
                    }

                    context ?.let {
                        NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                        Toast.makeText(context, "${notes.title} Saved Successfully !!", Toast.LENGTH_SHORT).show()
                        etNoteTitle.setText("")
                        etNoteDesc.setText("")
                        if (imp_links.visibility == View.VISIBLE) imp_links.visibility = View.GONE
                        if (layoutImage.visibility == View.VISIBLE) layoutImage.visibility = View.GONE
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

                if (!selectedImagePath.isNullOrBlank() && selectedImagePath.isNotEmpty()) {
                    notes.imgPath = selectedImagePath
                }

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                etNoteTitle.setText("")
                etNoteDesc.setText("")
                if (imp_links.visibility == View.VISIBLE) imp_links.visibility = View.GONE
                if (layoutImage.visibility == View.VISIBLE) layoutImage.visibility = View.GONE
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

    override fun onDetach() {
        super.onDetach()
        requireActivity().supportFragmentManager.popBackStack()
        replaceFragment(HomeFragment.newInstance(), false)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().supportFragmentManager.popBackStack()
        replaceFragment(HomeFragment.newInstance(), false)
    }

    private fun hasReadStoragePermission():Boolean {
        return EasyPermissions.hasPermissions(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun pickImageFromGallery(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, requestCodeImage)
        }
    }

    private fun readStorageTask() {
        if (hasReadStoragePermission()) {
            pickImageFromGallery()
        }
        else {
            EasyPermissions.requestPermissions(
                    requireActivity(),
                    getString(R.string.storage_permission_text),
                    readPermissionStorage,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath: String?
        var cursor = requireActivity().contentResolver.query(contentUri,null,null,null,null)
        if (cursor == null) {
            filePath = contentUri.path
        }
        else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }

        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeImage && resultCode == Activity.RESULT_OK){
            if (data != null){
                var selectedImageUrl = data.data
                if (selectedImageUrl != null){
                    try {
                        var inputStream = requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        layoutImage.visibility = View.VISIBLE
                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    }catch (e:Exception){
                        Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requireActivity())
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) { }

    override fun onRationaleDenied(requestCode: Int) { }
}