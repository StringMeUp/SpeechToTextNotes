package com.example.speechtotextnotes.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.speechtotextnotes.R
import com.example.speechtotextnotes.data.NoteList
import com.example.speechtotextnotes.databinding.NotesCardBinding

class NotesAdapter(private val list: MutableList<NoteList>) :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    class NotesViewHolder(private val binding: NotesCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteList) {
            note.notesList?.forEach {
                binding.note.text = it
                binding.noteId.text = note.id.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding: NotesCardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.notes_card, parent, false
        )
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun populateList(updatedList: MutableList<NoteList>) {
        list.clear()
        list.addAll(updatedList)
    }
}