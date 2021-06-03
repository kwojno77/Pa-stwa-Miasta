package com.example.pastwa_miasta.main_game

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import java.nio.file.WatchEvent

class InGameAdapter(
        var answers: ArrayList<Answer>,
) :
    RecyclerView.Adapter<InGameAdapter.ViewHolder>() {

    var isEditable: Boolean = true
    inner class ViewHolder(view: View, iTextChanged: ITextChange) : RecyclerView.ViewHolder(view) {
        var categoryLabel: TextView? = null
        var iTextChange : ITextChange? = null
        val userInput: EditText

        init {
            categoryLabel = view.findViewById(R.id.categoryLabel)
            userInput = view.findViewById(R.id.answer)
            iTextChange = iTextChanged
            userInput.addTextChangedListener(iTextChange)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_in_game, viewGroup, false)
        return ViewHolder(view, ITextChange())
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.iTextChange?.updatePosition(position)
        viewHolder.categoryLabel?.text = answers[position].category
        viewHolder.userInput.setText(answers[position].answer)
    }

    override fun getItemCount() = answers.size

    inner class ITextChange : TextWatcher {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            answers[position].answer = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}