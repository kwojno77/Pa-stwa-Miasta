package com.example.pastwa_miasta.main_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.results.ResultsActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Answer>

    private var myNick: String? = null
    private var gameId: String? = null

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        answersList = ArrayList()
        setViews()
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        myNick = intent.getStringExtra("myNick")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        getGameCategories()
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
        val restoredAllPictures = savedInstanceState.getParcelableArrayList<Answer>("answersList")
        if (restoredAllPictures != null) {
            answersList = restoredAllPictures
            (recyclerView.adapter as InGameAdapter).answers = restoredAllPictures
        }
    }

    fun onClick(view: View) {
        val i = Intent(this, ResultsActivity::class.java)
        startActivity(i)
    }
}