package com.example.pastwa_miasta.main_game.answers_voting

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.GameActivity
import com.example.pastwa_miasta.results.ResultsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class VotingActivity : AppCompatActivity() {

    var ended = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Reported>
    private lateinit var gameId: String
    private lateinit var myNick: String
    private lateinit var previousLetter: String
    private var currentRound = -1
    private var backPressed: Long = 0
    private lateinit var timerProgressBar: ProgressBar

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private var thread : VotingTimerThread = VotingTimerThread(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)
        timerProgressBar = findViewById(R.id.timerProgressBar)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        currentRound = intent.getIntExtra("currRound", -1)
        previousLetter = intent.getStringExtra("previousLetter").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        checkUser()
        setViews()
        getReported()
        timer()
    }

    private fun timer() {
        thread.start()
    }

    fun updateTime(progress : Float) {
        timerProgressBar.progress = (progress * 100).toInt()
    }

    fun endVoting() {
        ended = true
        calculateVotes()
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("gameId", gameId)
        i.putExtra("onlyResults", true)
        i.putExtra("previousLetter", previousLetter)
        startActivity(i)
        finish()
    }

    override fun onStop() {
        ended = true
        thread.running = false
        super.onStop()
        finish()
    }

    private fun checkUser() {
        val currUser = FirebaseAuth.getInstance().currentUser
        if (currUser == null) {
            ended = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            myNick = currUser.displayName.toString()
        }
    }

    private fun setViews() {
        recyclerView = findViewById(R.id.votingRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        answersList = ArrayList()
        val customAdapter = VotingAdapter(answersList)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private var itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
    object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val answer = answersList[viewHolder.adapterPosition]
            if(answer.wasAccepted == null) {
                if(direction == ItemTouchHelper.RIGHT) {
                    answer.wasAccepted = true
                    viewHolder.itemView.setBackgroundColor(
                        ContextCompat.getColor(applicationContext, R.color.correct_green))
                } else {
                    answer.wasAccepted = false
                    viewHolder.itemView.setBackgroundColor(
                        ContextCompat.getColor(applicationContext, R.color.wrong_red))
                }
                vote(answer)
            }
            recyclerView.adapter!!.notifyDataSetChanged()
        }
    }

    private fun vote(answer: Reported) {
        gameRef.child("Reported").child(answer.category).child(answer.playerNick)
            .child(answer.answer).child("Votes").child(myNick).setValue(answer.wasAccepted)
    }

    private fun getReported() {
        Thread.sleep(2000)
        gameRef.child("Reported")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val map = it.value as HashMap<*, *>
                    for(nick in map) {
                        if(nick.key == myNick)
                            continue
                        for(country in nick.value as HashMap<*, *>) {
                            answersList.add(
                                Reported(
                                    country.key as String,
                                    nick.key as String,
                                    it.key.toString()
                                )
                            )
                        }
                    }
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setAnswerTrue(category: String, answer: String) {
        gameRef.child("Players").child(myNick)
            .child(category).child(currentRound.toString()).child(answer).setValue("FULL_POINTS")
        gameRef.child("Players").child(myNick).child("Points").setValue(
            ServerValue.increment(10L))
        addKeywordToDatabase(category, answer)
    }

    private fun calculateVotes() {
        gameRef.child("Reported")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { categories ->
                        categories.children.forEach { players ->
                            if(players.key == myNick) {
                                players.children.forEach { answer ->
                                    var positiveVote = 0
                                    var negativeVote = 0
                                    answer.child("Votes").children.forEach { vote ->
                                        if(vote.value == true) positiveVote++
                                        else negativeVote++
                                    }
                                    if(positiveVote > negativeVote) {
                                        setAnswerTrue(categories.key.toString(), answer.key.toString())
                                    }
                                }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addKeywordToDatabase(category: String, answer: String) {
        db.reference.child("Keywords").child(category).child(answer.toLowerCase()).setValue(true)
    }

    override fun onBackPressed() {
        if (backPressed + 1000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                baseContext,
                "Jeśli wyjdziesz to już nie będziesz mógł dołączyć, czy na pewno tego chcesz?", Toast.LENGTH_SHORT
            )
                .show()
        }
        backPressed = System.currentTimeMillis()
    }
}