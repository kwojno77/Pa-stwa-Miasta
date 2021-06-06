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
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.example.pastwa_miasta.friendsRecyclerViewClick
import com.example.pastwa_miasta.waiting_room.IRecyclerViewClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.nio.file.WatchEvent

class FriendsAdapter (
    var players: ArrayList<Player>,
    private var friendsRecyclerViewClick: friendsRecyclerViewClick
) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playerLabel: TextView? = null
        var avatar: FloatingActionButton? = null
        init {
            playerLabel = view.findViewById(R.id.playerLabel)
            avatar = view.findViewById(R.id.imageView)
            avatar?.setOnClickListener { friendsRecyclerViewClick.onAvatarClicked(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FriendsAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_in_friends, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: FriendsAdapter.ViewHolder, position: Int) {
        val id: Int = position+1
        viewHolder.playerLabel?.text = players[position].name
    }

    override fun getItemCount() = players.size
}