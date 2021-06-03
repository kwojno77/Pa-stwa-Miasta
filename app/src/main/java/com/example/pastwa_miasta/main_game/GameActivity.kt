package com.example.pastwa_miasta.main_game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.answers_voting.VotingActivity
import com.example.pastwa_miasta.results.ResultsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Answer>
    private lateinit var timerView: TextView
    private lateinit var roundCounterView: TextView
    private lateinit var letterView: TextView

    private var currentRound: Int = 1
    private var maxRounds: Int = 0
    private lateinit var myNick: String
    private lateinit var gameId: String
    private lateinit var currentLetter: String
    private var isHost: Boolean = false
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
        isHost = intent.getBooleanExtra("isHost", false)
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        generateLetter()
        val restoredAllAnswers = savedInstanceState?.getParcelableArrayList<Answer>("answersList")
        if (restoredAllAnswers != null) {
            answersList = restoredAllAnswers
            (recyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
        } else {
            getGameCategories()
        }
        checkRounds()
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener { reportEnding() }
        timer()
    }

    private fun generateLetter() {
        val source = "ABCDEFGHIJKLMNOPRSTUWYZ"
        val letter = source[Random.nextInt(0, source.length)]
        gameRef.child("Rounds").child(currentRound.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    currentLetter = if(dataSnapshot.childrenCount <= 0) {
                        dataSnapshot.ref.child("letter").setValue(letter.toString())
                        dataSnapshot.ref.child("has_ended").setValue(false)
                        letter.toString()
                    } else
                        dataSnapshot.child("letter").value.toString()
                    letterView.text = currentLetter

                    if(dataSnapshot.child("stop_clicked").value == true) {
                        thread.changeTime(15)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun reportEnding() {
        (recyclerView.adapter as InGameAdapter).isEditable = false
        reportSlowRoundEnding()
        thread.changeTime(15)
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

    private fun checkRounds() {
        gameRef.child("Rounds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    if(it.value == true) {
                        currentRound++
                    }
                }
                maxRounds = dataSnapshot.children.count()
                if(currentRound > maxRounds) {
                    showGameResults()
                    return
                }
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
        timerView.text = timeString
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
        letterView = findViewById(R.id.letterView)
        roundCounterView = findViewById(R.id.roundCounterView)
        timerView = findViewById(R.id.timerView)
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
        i.putExtra("gameId", gameId)
        startActivity(i)
        finish()
    }

    private fun sendAnswersToDatabase() {
        for(answer in answersList) {
            var answer1 = answer.answer
            if(answer1.isEmpty())
                answer1 = "-"
            gameRef.child("Players").child(myNick)
                .child(answer.category).child(currentRound.toString()).child(answer1).setValue("check")
        }
    }

    private fun showVoting() {
        val i = Intent(this, VotingActivity::class.java)
        i.putExtra("gameId", gameId)
        i.putExtra("currRound", currentRound)
        startActivity(i)
        finish()
    }

    private fun verifyInDatabase() {
        db.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var playerRef = dataSnapshot.child("Games").child(gameId).child("Players").child(myNick)
                playerRef.children.forEach {
                    if(it.key.toString() != "Points") {
                        val map: HashMap<String, String> =
                            (it.value as ArrayList<HashMap<String, String>>).last()
                        for (elem in map) {
                            var isCorrect =
                                dataSnapshot.child("Keywords").child(it.key!!).child(elem.key.toLowerCase())
                                    .exists() && elem.key.toLowerCase()[0].toString() == currentLetter.toLowerCase()
                            setAnswerTrueOrFalse(it.key!!, elem.key, isCorrect)
                        }
                    }
                }
                autoReport()
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setAnswerTrueOrFalse(category: String, answer: String, value: Boolean) {
        gameRef.child("Players").child(myNick)
            .child(category).child(currentRound.toString()).child(answer).setValue(value)
    }

    private fun reportToDatabase(category: String, answer: String) {
        if (answer.trim().length > 1 && answer.trim()[0].toString().equals(currentLetter, ignoreCase = true))
            gameRef.child("Reported").child(category).child(myNick)
                .child(answer).child("Votes").setValue("-")
    }

    private fun autoReport() {
        gameRef.child("Players").child(myNick)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    if(it.key.toString() != "Points") {
                        val map: HashMap<String, String> =
                            (it.value as ArrayList<HashMap<String, String>>).last()
                        for (elem in map) {
                            if (dataSnapshot.child(it.key!!).child(currentRound.toString())
                                    .child(elem.key).value == false)
                                reportToDatabase(it.key!!, elem.key)
                        }
                    }
                }
                showVoting()
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun reportSlowRoundEnding() {
        gameRef.child("Rounds").child(currentRound.toString()).child("stop_clicked").setValue(true)
    }

    fun endRound() {
        sendAnswersToDatabase()
        verifyInDatabase()
        thread.running = false
    }
}