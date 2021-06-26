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
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.waiting_room.IRecyclerViewClick
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ResultsActivity : AppCompatActivity(), IRecyclerViewClick {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView

    private lateinit var gameId: String
    private lateinit var myNick: String
    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)

        checkUser()
        playersList = ArrayList()
        viewsInit()
        getResultsFromDatabase()
    }

    private fun viewsInit() {
        recyclerView = findViewById(R.id.recyclerViewResult)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = ResultsAdapter(playersList, this)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.letterView)
        playerCounterView.text = "Podsumowanie"
    }

    private fun viewProfile(nick: String) {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", nick)
        startActivity(i)
    }

    private fun getResultsFromDatabase() {
        gameRef.child("Players")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    var player = Player(it.key!!)
                    player.points = (it.child("Points").value as Long).toInt()
                    playersList.add(player)
                }
                playersList.sortByDescending { it.points }
                recyclerView.adapter!!.notifyDataSetChanged()
                updateStats()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateStats() {
        gameRef.child("Players").child(myNick)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var newPoints = dataSnapshot.child("Points").value as Long
                    db.reference.child("Users").child(myNick).child("Stats").child("Points").
                    setValue(ServerValue.increment(newPoints))
                    if(newPoints == playersList[0].points.toLong()) {
                        db.reference.child("Users").child(myNick).child("Stats").child("WonGames")
                            .setValue(ServerValue.increment(1L))
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkUser() {
        val currUser = FirebaseAuth.getInstance().currentUser
        if (currUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            myNick = currUser.displayName.toString()
        }
    }

    override fun onJoinedAvatarClicked(pos: Int) {
        viewProfile(playersList[pos].name)
    }

    override fun onInvitedAvatarClicked(adapterPosition: Int) {}
}

