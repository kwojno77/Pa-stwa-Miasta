package com.example.pastwa_miasta.waiting_room

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.main_game.GameActivity

class RoomActivity : AppCompatActivity(), IRecyclerViewClick {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        recyclerView = findViewById(R.id.recyclerViewRoom)
        recyclerView.layoutManager = LinearLayoutManager(this)
        playersList = ArrayList()
        playersList.add(Player("Wojtek"))
        playersList.add(Player("Kacper"))
        val customAdapter = RoomAdapter(playersList, this)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.playerCounterLabel)
        playerCounterView.text = "${playersList.size} graczy"
    }

    override fun onItemClick(pos: Int) {
        Toast.makeText(this, "Kliknięto w gracza ${playersList[pos].name}", Toast.LENGTH_SHORT).show()
        playersList.add(Player("Wiktoria ${playersList.size}"))
        refresh()
    }

    private fun refresh() {
        playerCounterView.text = "${playersList.size} graczy"
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putParcelableArrayList("playersList", java.util.ArrayList(playersList))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredAllPictures = savedInstanceState.getParcelableArrayList<Player>("playersList")
        if (restoredAllPictures != null) {
            playersList = restoredAllPictures
            (recyclerView.adapter as RoomAdapter).players = restoredAllPictures
        }
        refresh()
    }

    fun startGame(view: View) {
        val i = Intent(this, GameActivity::class.java)
        startActivity(i)
    }
}