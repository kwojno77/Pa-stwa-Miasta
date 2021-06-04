package com.example.pastwa_miasta.main_game.answers_voting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.example.pastwa_miasta.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class VotingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Reported>
    private lateinit var gameId: String
    private lateinit var myNick: String

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        checkUser()
        setViews()
        getReported()

        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }
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
            var answer = answersList[viewHolder.adapterPosition]
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
        gameRef.child("Reported").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val map = it.value as HashMap<*, *>
                    for(nick in map) {
                        for (country in nick.value as HashMap<*, *>) {
                            if (nick.key == myNick)
                                continue
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

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }
}