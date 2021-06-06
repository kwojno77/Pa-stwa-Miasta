package com.example.pastwa_miasta.waiting_room

import android.widget.Button

interface FriendsListRecyclerViewClick {
    fun onAvatarClicked(adapterPosition: Int)
    fun onButtonClicked(adapterPosition: Int, button: Button)
}