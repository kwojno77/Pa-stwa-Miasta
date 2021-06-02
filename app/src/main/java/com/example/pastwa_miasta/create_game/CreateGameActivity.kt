package com.example.pastwa_miasta.create_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.waiting_room.RoomActivity

class CreateGameActivity : AppCompatActivity() {
    //private lateinit var db: FirebaseDatabase
    //private lateinit var gameRef: DatabaseReference
    private var context = this
    private var listData : MutableList<SpinnerModel> = ArrayList()
    private  lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)

        //var gameId = "1" // TYMCZASOWE
        //db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        //gameRef = db.reference.child("Games").child(gameId!!)
        prepareAdapter()
        prepareSpinners()
    }

    // Prepares recyclerView adapter
    private fun prepareAdapter() {
        recyclerView = findViewById(R.id.spinnerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CategorySpinnerAdapter(listData, this) { position -> }
        addCategorySpinner()
    }

    private fun addCategorySpinner() {
        val spinner : Spinner = Spinner(this)
        val s = SpinnerModel()
        s.spinner = spinner
        listData.add(s)
    }

    // Prepares Spinners' Adapters
    private fun prepareSpinners() {
        val gameModeSpinner: Spinner = findViewById(R.id.gameModeSpinner)
        val roundNumSpinner: Spinner = findViewById(R.id.roundNumSpinner)
        val categoryNumSpinner: Spinner = findViewById(R.id.categoryNumSpinner)
        createSpinner(gameModeSpinner, R.array.gamemode)
        createSpinner(roundNumSpinner, R.array.roundsNum)
        createSpinner(categoryNumSpinner, R.array.categoryNum)
        categoryNumSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { return}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                listData.clear()
                for (i in 0 until categoryNumSpinner.selectedItem.toString().toInt()) {
                    addCategorySpinner()
                }
                recyclerView.adapter!!.notifyDataSetChanged()
            }
        }
    }

    // Creates a Spinner by getting Spinner object and a resource array
    private fun createSpinner(spinner: Spinner, array: Int) {
        ArrayAdapter.createFromResource(
            this,
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    // Button takes you to a room activity
    fun confirm(view: View) {
        val i = Intent(this, RoomActivity::class.java)
        i.putExtra("isHost", true)
        startActivity(i)
    }
}