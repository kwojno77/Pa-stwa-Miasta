package com.example.pastwa_miasta.main_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.answers_voting.VotingActivity
import com.example.pastwa_miasta.results.ResultsActivity
import com.example.pastwa_miasta.waiting_room.IRecyclerViewClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random


class GameActivity : AppCompatActivity(), IRecyclerViewClick {

    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var playersPointsRecyclerView: RecyclerView
    private lateinit var playersAnswerRecyclerView: RecyclerView
    private lateinit var myAnswersList: ArrayList<Answer>
    private lateinit var otherAnswersList: ArrayList<Answer>
    private lateinit var playersPointsList: ArrayList<Player>
    private lateinit var timerView: TextView
    private lateinit var roundCounterView: TextView
    private lateinit var letterView: TextView
    private lateinit var stopButton: FloatingActionButton
    private lateinit var timerProgressBar: ProgressBar

    private var backPressed: Long = 0
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
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        onlyResults = intent.getBooleanExtra("onlyResults", false)
        isHost = intent.getBooleanExtra("isHost", false)
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        myAnswersList = ArrayList()
        playersPointsList = ArrayList()
        otherAnswersList = ArrayList()
        setViews()
        checkUser()
        checkRounds()
        if(onlyResults) {
            previousLetter = intent.getStringExtra("previousLetter").toString()
            playersAnswerRecyclerView.visibility = View.VISIBLE
            letterView.text = previousLetter
            timerView.visibility = View.INVISIBLE
            stopButton.visibility = View.GONE
            timerProgressBar.visibility = View.VISIBLE
            resultsTimer()
            return
        }

        val restoredAllAnswers = savedInstanceState?.getParcelableArrayList<Answer>("myAnswersList")
        if (restoredAllAnswers != null) {
            myAnswersList = restoredAllAnswers
            (gameRecyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
        } else {
            getGameCategories()
        }
        timer()
    }

    private fun setRepeatedAnswers() {
        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.child("Players").child(myNick).children.forEach { category ->
                    if (category.key != "Points") {
                        category.child(currentRound.toString()).children.forEach { answer ->
                            dataSnapshot.child("Answers").child(category.key!!).children.forEach { allAnswers->
                                if(allAnswers.key.toString() != myNick) {
                                    if(answer.key.toString()
                                            .equals(
                                                allAnswers.value.toString(),
                                                ignoreCase = true
                                            ) && answer.value != "REPEATED"
                                    ) {
                                        answer.ref.setValue("REPEATED")
                                        dataSnapshot.child("Players").child(myNick)
                                            .child("Points").ref.setValue(
                                                ServerValue.increment(
                                                    -5L
                                                )
                                            )

                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(1000)
                getPlayersPointsFromDatabase()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
                                    myAnswersList.add(answer)
                            }
                        }
                    }
                    gameRecyclerView.adapter!!.notifyDataSetChanged()
                    getOtherPlayersAnswersFromDatabase()
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
                        Toast.makeText(baseContext,"Zostało 15s!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        gameRef.child("Reported").removeValue()
        gameRef.child("Answers").removeValue()
        getPlayersPointsFromDatabase()
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
        (gameRecyclerView.adapter as InGameAdapter).isEditable = false
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
                    return
                }
                setRoundLabel()
                if(!onlyResults) generateLetter()
                else {
                    setRepeatedAnswers()
                }
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
        if(seconds == 15) {
            reportSlowRoundEnding()
        }
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
                        myAnswersList.add(Answer(it.key.toString()))
                    }
                    gameRecyclerView.adapter?.notifyDataSetChanged()
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
        gameRecyclerViewInit()
        playersPointsRecyclerViewInit()
        playersAnswerRecyclerViewInit()
    }

    private fun gameRecyclerViewInit() {
        gameRecyclerView = findViewById(R.id.recyclerViewGame)
        gameRecyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = InGameAdapter(myAnswersList, this, !onlyResults)
        gameRecyclerView.adapter = customAdapter
        gameRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun playersPointsRecyclerViewInit() {
        playersPointsRecyclerView = findViewById(R.id.playersPointsRecyclerViewer)
        playersPointsRecyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = PlayersPointsAdapter(playersPointsList)
        playersPointsRecyclerView.adapter = customAdapter
        playersPointsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun playersAnswerRecyclerViewInit() {
        playersAnswerRecyclerView = findViewById(R.id.playersAnswersRecyclerView)
        playersAnswerRecyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = InGameAdapter(otherAnswersList, this, !onlyResults)
        playersAnswerRecyclerView.adapter = customAdapter
        playersAnswerRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun getPlayersPointsFromDatabase() {
        gameRef.child("Players")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    playersPointsList.clear()
                    dataSnapshot.children.forEach {
                        val player = Player(it.key!!)
                        player.points = (it.child("Points").value as Long).toInt()
                        playersPointsList.add(player)
                    }
                    playersPointsRecyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        if(onlyResults) getResultsFromDatabase()
    }

    private fun getOtherPlayersAnswersFromDatabase() {
        gameRef.child("Players")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    otherAnswersList.clear()
                    dataSnapshot.children.forEach { player ->
                        if (player.key.toString() != myNick) {
                            val answer = Answer(player.key.toString())
                            otherAnswersList.add(answer)
                            player.children.forEach { category ->
                                category.child(currentRound.toString()).children.forEach { ans ->
                                    val answer = Answer(category.key.toString())
                                    answer.author = player.key.toString()
                                    answer.isAccepted = AnswerState.valueOf(ans.value.toString())
                                    answer.answer = ans.key.toString()
                                    otherAnswersList.add(answer)
                                }
                            }
                        }
                    }
                    playersAnswerRecyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putParcelableArrayList("myAnswersList", java.util.ArrayList(myAnswersList))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredAllAnswers = savedInstanceState.getParcelableArrayList<Answer>("myAnswersList")
        if (restoredAllAnswers != null) {
            myAnswersList = restoredAllAnswers
            (gameRecyclerView.adapter as InGameAdapter).answers = restoredAllAnswers
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
        for(answer in myAnswersList) {
            var answer1 = answer.answer.trim()
            if(answer1.isEmpty())
                answer1 = "-"
            gameRef.child("Players").child(myNick)
                .child(answer.category).child(currentRound.toString()).child(answer1).setValue("UNKNOWN")
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
                                dataSnapshot.child("Keywords").child(it.key!!).child(elem.key.toLowerCase(
                                    Locale.ROOT
                                )).exists() && elem.key.toLowerCase(Locale.ROOT)[0].toString() == currentLetter.toLowerCase(
                                    Locale.ROOT)
                            setAnswerTrueOrFalse(it.key!!, elem.key, isCorrect)
                        }
                    }
                }
                autoReport()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setAnswersInAnswers(answer: String, category: String) {
        gameRef.child("Answers").child(category).child(myNick).setValue(answer.toLowerCase(Locale.ROOT).trim())
    }

    private fun setAnswerTrueOrFalse(
        category: String,
        answer: String,
        isCorrect: Boolean,
    ) {
        val value: String = if(isCorrect) {
            gameRef.child("Players").child(myNick).child("Points").setValue(
                ServerValue.increment(10L))
            setAnswersInAnswers(answer, category)
            "FULL_POINTS"
        } else
            "WRONG"
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

    override fun onJoinedAvatarClicked(pos: Int) {}

    override fun onInvitedAvatarClicked(adapterPosition: Int) {}

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