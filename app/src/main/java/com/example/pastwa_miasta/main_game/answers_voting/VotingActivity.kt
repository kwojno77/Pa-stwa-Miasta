package com.example.pastwa_miasta.main_game.answers_voting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.example.pastwa_miasta.R
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class VotingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Reported>
    private lateinit var gameId: String

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)

        setViews()
        getReported()
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
            if(answersList[viewHolder.adapterPosition].wasAccepted == null) {
                if(direction == ItemTouchHelper.RIGHT) {
                    answersList[viewHolder.adapterPosition].wasAccepted = true
                    viewHolder.itemView.setBackgroundColor(
                        ContextCompat.getColor(applicationContext, R.color.correct_green))
                } else {
                    answersList[viewHolder.adapterPosition].wasAccepted = false
                    viewHolder.itemView.setBackgroundColor(
                        ContextCompat.getColor(applicationContext, R.color.wrong_red))
                }
            }
            recyclerView.adapter!!.notifyDataSetChanged()
        }
    }

    private fun getReported() {
        gameRef.child("Reported").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val map: HashMap<String, String> = it.value as HashMap<String, String>
                    for(el in map)
                        answersList.add(Reported(el.value, el.key, it.key.toString()))
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}