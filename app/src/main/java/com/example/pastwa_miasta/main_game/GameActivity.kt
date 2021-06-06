package com.example.pastwa_miasta.main_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
import kotlin.random.Random


class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Answer>
    private lateinit var timerView: TextView
    private lateinit var roundCounterView: TextView
    private lateinit var letterView: TextView
    private lateinit var stopButton: FloatingActionButton
    private lateinit var timerProgressBar: ProgressBar

    private var currentRound: Int = 1
    private var maxRounds: Int = 0
    private lateinit var myNick: String
    private lateinit var gameId: String
    private lateinit var currentLetter: String
    private lateinit var previousLetter: String
    private var isHost: Boolean = false
    private var thread : TimerThread = TimerThread(this)
    private var onlyResults: Boolean = false
    var ended: Boolean = false
    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private var resultsThread : ResultsTimerThread = ResultsTimerThread(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        answersList = ArrayList()
        setViews()
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        checkUser()
        onlyResults = intent.getBooleanExtra("onlyResults", false)
        isHost = intent.getBooleanExtra("isHost", false)
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        previousLetter = intent.getStringExtra("previousLetter").toString()
        checkRounds()
        if(onlyResults) {
            letterView.text = previousLetter
            timerView.visibility = View.INVISIBLE
            stopButton.visibility = View.INVISIBLE

            timerProgressBar.visibility = View.VISIBLE
            resultsTimer()
            return
        }

        val restoredAllAnswers = savedInstanceState?.getParcelableArrayList<Answer>("answersList")
        if (restoredAllAnswers != null) {
            answersList = restoredAllAnswers
            (recyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
        } else {
            getGameCategories()
        }
        timer()
    }

    private fun getResultsFromDatabase() {
        gameRef.child("Players").child(myNick)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        if(it.key != "Points") {
                            dataSnapshot.child(it.key.toString())
                                .child(currentRound.toString()).children.forEach { answerIt->
                                    val answer = Answer(it.key.toString())
                                    answer.answer = answerIt.key.toString()
                                    answer.isAccepted = AnswerState.valueOf(answerIt.value.toString())
                                    answersList.add(answer)
                            }
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun generateLetter() {
        val source = "ABCDEFGHIJKLMNOPRSTUWZ"
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

                    if(dataSnapshot.hasChild("stop_clicked") && dataSnapshot.child("stop_clicked").value == true) {
                        thread.changeTime(15)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onStop() {
        ended = true
        resultsThread.running = false
        thread.running = false
        super.onStop()
        finish()
    }

    private fun reportEnding() {
        if(thread.time <= 15) return
        (recyclerView.adapter as InGameAdapter).isEditable = false
        reportSlowRoundEnding()
        thread.changeTime(15)
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

    private fun checkRounds() {
        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                maxRounds = (dataSnapshot.child("Settings").child("Rounds_num").value as Long).toInt()
                currentRound = (dataSnapshot.child("CurrentRound").value as Long).toInt()
                if(currentRound > maxRounds) {
                    showGameResults()
                    updateStats()
                    return
                }
                setRoundLabel()
                if(!onlyResults) generateLetter()
                else getResultsFromDatabase()
            }
            override fun onCancelled(error: DatabaseError) {}})
    }

    private fun setRoundLabel() {
        roundCounterView.text = "Runda $currentRound/$maxRounds"
    }

    private fun timer() {
        thread.start()
    }

    private fun resultsTimer() {
        resultsThread.start()
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

    fun updateProgressBar(progress : Float) {
        timerProgressBar.progress = (progress * 100).toInt()
    }

    fun endResults() {
        if(ifWasIsLastRound()){
            showGameResults()
            return
        }
        db.reference.child("Games").child(gameId).child("CurrentRound").setValue(currentRound + 1)
        ended = true
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("gameId", gameId)
        i.putExtra("onlyResults", false)
        startActivity(i)
        finish()
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
        timerProgressBar = findViewById((R.id.timerProgressBar))
        stopButton = findViewById(R.id.floatingActionButton)
        stopButton.setOnClickListener { reportEnding() }
        recyclerView = findViewById(R.id.recyclerViewGame)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = InGameAdapter(answersList, this)
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
        ended = true
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
        ended = true
        val i = Intent(this, VotingActivity::class.java)
        i.putExtra("gameId", gameId)
        i.putExtra("currRound", currentRound)
        i.putExtra("previousLetter", currentLetter)
        startActivity(i)
        finish()
    }

    private fun verifyInDatabase() {
        db.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val playerRef = dataSnapshot.child("Games").child(gameId).child("Players").child(myNick)
                playerRef.children.forEach {
                    if(it.key.toString() != "Points") {
                        val map: HashMap<String, String> =
                            (it.value as ArrayList<HashMap<String, String>>).last()
                        for (elem in map) {
                            val isCorrect =
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

    private fun setAnswerTrueOrFalse(category: String, answer: String, isCorrect: Boolean) {
        val value: String
        if(isCorrect) {
            value = "FULL_POINTS"
            gameRef.child("Players").child(myNick).child("Points").setValue(
                ServerValue.increment(10L))
        }
        else
            value = "WRONG"
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
                                    .child(elem.key).value == "WRONG")
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
        gameRef.child("Rounds").child(currentRound.toString())
            .child("stop_clicked").setValue(true)
    }

    fun endRound() {
        thread.running = false
        sendAnswersToDatabase()
        verifyInDatabase()
    }

    private fun ifWasIsLastRound(): Boolean {
        return currentRound == maxRounds
    }

    private fun updateStats() {
        var maximumPoints = 0
        val currentPoints: MutableMap<String, Int> = mutableMapOf()
        gameRef.child("Players")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        val player = it.key
                        val points = (it.child("Points").value as Long).toInt()
                        if (player != null) {
                            currentPoints[player] = points
                        }
                        if (points >= maximumPoints) {
                            maximumPoints = points
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        db.reference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        if (currentPoints.containsKey(it.key)) {
                            var userPoints = (it.child("Stats").child("Points").value as Long).toInt()
                            userPoints += currentPoints[it.key]!!
                            it.key?.let { it1 -> db.reference.child("Users").child(it1).child("Stats").child("Points").setValue(userPoints) }
                            if (currentPoints[it.key]!! == maximumPoints) {
                                var wonGames = (it.child("Stats").child("WonGames").value as Long).toInt()
                                wonGames += 1
                                it.key?.let { it1 -> db.reference.child("Users").child(it1).child("Stats").child("WonGames").setValue(wonGames) }
                            }
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}