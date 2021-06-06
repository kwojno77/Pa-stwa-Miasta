package com.example.pastwa_miasta.waiting_room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsListAdapter (
    var players: ArrayList<Player>,
    private var friendsListRecyclerViewClick: FriendsListRecyclerViewClick
) :
    RecyclerView.Adapter<FriendsListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playerLabel: TextView? = null
        var avatar: FloatingActionButton? = null
        var button: Button? = null
        init {
            playerLabel = view.findViewById(R.id.playerLabel)
            avatar = view.findViewById(R.id.imageView)
            avatar?.setOnClickListener { friendsListRecyclerViewClick.onAvatarClicked(adapterPosition) }
            button = view.findViewById<Button>(R.id.friendsButton)
            button?.setOnClickListener { friendsListRecyclerViewClick.onButtonClicked(adapterPosition,
                button!!
            ) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_in_friends_list, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val id: Int = position+1
        viewHolder.playerLabel?.text = players[position].name
    }

    override fun getItemCount() = players.size
}