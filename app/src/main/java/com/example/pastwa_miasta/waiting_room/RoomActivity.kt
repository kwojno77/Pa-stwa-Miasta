package com.example.pastwa_miasta.waiting_room

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.main_game.GameActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RoomActivity : AppCompatActivity(), IRecyclerViewClick {
    private lateinit var recyclerView: RecyclerView
    private lateinit var playersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView

    private var myNick: String? = null
    private var gameId: String? = null

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        myNick = intent.getStringExtra("myNick")
        //gameId = intent.getStringExtra("gameId").toString()
        gameId = "1"
        gameRef = db.reference.child("Games").child(gameId!!)
        playersList = ArrayList()

        findViewById<Button>(R.id.button).setOnClickListener {
            startGame()
        }

        setViews()
        listenForJoiningPlayers()
        listenForGameStart()
    }

    private fun setViews() {
        recyclerView = findViewById(R.id.recyclerViewRoom)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = RoomAdapter(playersList, this)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.playerCounterLabel)
        playerCounterView.text = "${playersList.size} graczy"
    }

    private fun listenForJoiningPlayers() {
        gameRef.child("Players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                playersList.clear()
                dataSnapshot.children.forEach {
                    playersList.add(Player(it.key.toString()))
                }
                refresh()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun listenForGameStart() {
        gameRef.child("Game_flag").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value == true)
                    startGame()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onItemClick(pos: Int) {
        refresh()
    }

    @SuppressLint("SetTextI18n")
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
        if(restoredAllPictures != null) {
            playersList = restoredAllPictures
            (recyclerView.adapter as RoomAdapter).players = restoredAllPictures
        }
        refresh()
    }

    fun startGame() {
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("myNick", myNick)
        i.putExtra("gameId", gameId)
        startActivity(i)
        finish()
    }
}