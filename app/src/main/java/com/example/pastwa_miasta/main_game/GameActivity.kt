package com.example.pastwa_miasta.main_game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.results.ResultsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.max

class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Answer>
    private lateinit var timerView: TextView
    private lateinit var roundCounterView: TextView

    private var currentRound: Int = 0
    private var maxRounds: Int = 0
    private var myNick: String? = null
    private var gameId: String? = null
    private var thread : TimerThread = TimerThread(this)

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        answersList = ArrayList()
        setViews()
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        checkUser()

        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)

        roundCounterView = findViewById(R.id.roundCounterView)
        timerView = findViewById(R.id.timerView)

        val restoredAllAnswers = savedInstanceState?.getParcelableArrayList<Answer>("answersList")
        if (restoredAllAnswers != null) {
            answersList = restoredAllAnswers
            (recyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
        } else {
            getGameCategories()
        }
        checkRounds()
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener { endRound() }
        timer()
    }

    // Jeszcze nie wiem czy to potrzebne ale gdzieś może będzie wykorzystane
    private fun checkUser() {
        var currUser = FirebaseAuth.getInstance().currentUser
        if (currUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            myNick = currUser.displayName
        }
    }

    private fun checkRounds() {
        gameRef.child("Rounds").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentRound = dataSnapshot.children.count()
                checkMaxRounds()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkMaxRounds() {
        gameRef.child("Settings").child("Rounds_num").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                maxRounds = dataSnapshot.value.toString().toInt()
                setRoundLabel()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setRoundLabel() {
        roundCounterView.text = "Runda $currentRound/$maxRounds"
    }

    private fun timer() {
        thread.start()
    }

    fun updateTime(time : Int) {
        var minutes = 0
        var seconds = time
        while (seconds - 60 >= 0) {
            minutes++
            seconds -= 60
        }
        var timeString = "$minutes:$seconds"
        if (seconds < 10) {
            timeString = "$minutes:0$seconds"
        }
        findViewById<TextView>(R.id.timerView).text = timeString
    }

    private fun getGameCategories() {
        gameRef.child("Settings").child("Categories")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach {
                            answersList.add(Answer(it.key.toString()))
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun setViews() {
        recyclerView = findViewById(R.id.recyclerViewGame)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = InGameAdapter(answersList)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putParcelableArrayList("answersList", java.util.ArrayList(answersList))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredAllAnswers = savedInstanceState.getParcelableArrayList<Answer>("answersList")
        if (restoredAllAnswers != null) {
            answersList = restoredAllAnswers
            (recyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
        }
    }

    private fun showGameResults() {
        val i = Intent(this, ResultsActivity::class.java)
        startActivity(i)
        finish()
    }

    fun endRound() {
        thread.running = false
        Log.d("PM2021", "Round Ends")
        showGameResults()
        //TODO po naciśnięciu  przycisku lub jak czas się skończy
    }
}