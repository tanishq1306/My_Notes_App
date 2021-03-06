package com.example.mynotesapp.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesapp.R
import com.example.mynotesapp.database.Note
import kotlinx.android.synthetic.main.note_item_layout.view.*

class NotesAdapter() :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    var listener:OnItemClickListener? = null
    var arrList = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    fun setData(arrNotesList: List<Note>) {
        arrList = arrNotesList as ArrayList<Note>
    }

    fun setOnClickListener(listener1: OnItemClickListener){
        listener = listener1
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.textTitle.text = arrList[position].title
        holder.itemView.textContent.text = arrList[position].noteText
        holder.itemView.textDateAndTime.text = arrList[position].dateTime

        holder.itemView.layoutNote.setOnClickListener{
            listener!!.onClicked(arrList[position].id!!)
        }
    }

    class NotesViewHolder(view:View) : RecyclerView.ViewHolder(view) { }

    interface OnItemClickListener{
        fun onClicked(noteId:Int)
    }
}