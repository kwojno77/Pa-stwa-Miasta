package com.example.pastwa_miasta.main_game.answers_voting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R


class VotingAdapter(
    var answers: ArrayList<Reported>
) :
    RecyclerView.Adapter<VotingAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var reportLabel: TextView? = null
        init {
            reportLabel = view.findViewById(R.id.reportLabel)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_of_reports, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val report = answers[position]
        viewHolder.reportLabel?.text = "${report.category} | ${report.playerNick} | ${report.answer}"
    }

    override fun getItemCount() = answers.size

}