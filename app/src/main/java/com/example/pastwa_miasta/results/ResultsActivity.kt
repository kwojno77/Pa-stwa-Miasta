package com.example.pastwa_miasta.results

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R

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
        playerCounterView = findViewById(R.id.textView)
        playerCounterView.text = "Podsumowanie"
    }

}