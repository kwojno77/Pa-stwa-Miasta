package com.example.pastwa_miasta.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R

class ResultsAdapter(
        var players: ArrayList<Player>
) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playerLabel: TextView? = null
        init {
            playerLabel = view.findViewById(R.id.playerLabel)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_of_players, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.playerLabel?.text = players[position].name + " - " + players[position].points
    }

    override fun getItemCount() = players.size
}