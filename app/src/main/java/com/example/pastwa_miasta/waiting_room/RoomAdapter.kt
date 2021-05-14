package com.example.pastwa_miasta.waiting_room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R

class RoomAdapter(
        var players: ArrayList<Player>,
        private var iRecycleViewClick: IRecyclerViewClick,
) :
    RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playerLabel: TextView? = null
        init {
            playerLabel = view.findViewById(R.id.playerLabel)
            view.setOnClickListener { iRecycleViewClick.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_of_players, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val id: Int = position+1
        viewHolder.playerLabel?.text = "#$id  -  " + players[position].name
    }

    override fun getItemCount() = players.size
}