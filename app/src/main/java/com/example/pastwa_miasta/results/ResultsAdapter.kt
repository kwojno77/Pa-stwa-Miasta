package com.example.pastwa_miasta.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.waiting_room.IRecyclerViewClick
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ResultsAdapter(
    var players: ArrayList<Player>,
    private var iRecycleViewClick: IRecyclerViewClick,
) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playerLabel: TextView? = null
        var avatar: FloatingActionButton? = null
        init {
            playerLabel = view.findViewById(R.id.playerLabel)
            avatar = view.findViewById(R.id.imageView)
            avatar?.setOnClickListener { iRecycleViewClick.onJoinedAvatarClicked(adapterPosition) }
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