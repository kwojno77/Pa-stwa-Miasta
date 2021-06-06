package com.example.pastwa_miasta.main_game

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R

class InGameAdapter(
        var answers: ArrayList<Answer>,
        private var context: Context,
        var isEditable: Boolean
) :
    RecyclerView.Adapter<InGameAdapter.ViewHolder>() {

    inner class ViewHolder(view: View, iTextChanged: ITextChange) : RecyclerView.ViewHolder(view) {
        var categoryLabel: TextView? = null
        var iTextChange : ITextChange? = null
        val userInput: EditText

        init {
            categoryLabel = view.findViewById(R.id.categoryLabel)
            userInput = view.findViewById(R.id.answer)
            userInput.isEnabled = isEditable
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
        var ans = answers[position]
        viewHolder.iTextChange?.updatePosition(position)
        viewHolder.categoryLabel?.text = ans.category
        viewHolder.userInput.setText(ans.answer)
        if(ans.isAccepted == AnswerState.WRONG)
            viewHolder.userInput.setBackgroundColor(
                ContextCompat.getColor(context, R.color.wrong_red))
        if(ans.isAccepted == AnswerState.FULL_POINTS)
            viewHolder.userInput.setBackgroundColor(
                ContextCompat.getColor(context, R.color.correct_green))
        if(ans.isAccepted == AnswerState.REPEATED)
            viewHolder.userInput.setBackgroundColor(
                ContextCompat.getColor(context, R.color.repeated_yellow))
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