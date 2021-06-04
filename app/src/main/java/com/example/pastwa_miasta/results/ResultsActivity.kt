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

class ResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        recyclerView = findViewById(R.id.recyclerViewResult)
        recyclerView.layoutManager = LinearLayoutManager(this)
        playersList = ArrayList()
        playersList.add(Player("Wojtek"))
        playersList.add(Player("Kacper"))
        val customAdapter = ResultsAdapter(playersList)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.letterView)
        playerCounterView.text = "Podsumowanie"

        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }
    }

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }
}