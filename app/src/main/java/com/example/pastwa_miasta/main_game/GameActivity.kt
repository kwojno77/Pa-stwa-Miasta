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

class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var answersList: ArrayList<Answer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        recyclerView = findViewById(R.id.recyclerViewGame)
        recyclerView.layoutManager = LinearLayoutManager(this)
        answersList = ArrayList()
        answersList.add(Answer("Państwa"))
        answersList.add(Answer("Miasta"))
        answersList.add(Answer("Rzecz"))
        answersList.add(Answer("Imię"))
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