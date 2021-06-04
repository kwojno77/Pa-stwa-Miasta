package com.example.pastwa_miasta.results

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView

    private lateinit var gameId: String
    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)

        playersList = ArrayList()
        viewsInit()
        getResultsFromDatabase()
    }

    private fun viewsInit() {
        recyclerView = findViewById(R.id.recyclerViewResult)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = ResultsAdapter(playersList)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.letterView)
        playerCounterView.text = "Podsumowanie"
    }

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }

    private fun getResultsFromDatabase() {
        gameRef.child("Players")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    var player = Player(it.key!!)
                    player.points = it.child("Points").value as Int
                    playersList.add(player)
                }
                recyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}