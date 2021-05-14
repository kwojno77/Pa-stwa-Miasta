package com.example.pastwa_miasta.main_game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R

class InGameAdapter(
        var answers: ArrayList<Answer>,
) :
    RecyclerView.Adapter<InGameAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var categoryLabel: TextView? = null
        var userInput: EditText? = null
        init {
            categoryLabel = view.findViewById(R.id.categoryLabel)
            userInput = view.findViewById(R.id.answer)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_in_game, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.categoryLabel?.text = answers[position].category
        viewHolder.userInput?.setText(answers[position].answer)
    }

    override fun getItemCount() = answers.size
}