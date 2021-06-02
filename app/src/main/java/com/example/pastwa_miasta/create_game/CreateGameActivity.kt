package com.example.pastwa_miasta.create_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.waiting_room.RoomActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateGameActivity : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private var listData : MutableList<SpinnerModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var roundNumSpinner: Spinner
    private lateinit var categoryNumSpinner: Spinner

    private lateinit var myNick: String
    private lateinit var gameId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = db.reference
        checkUser()
        prepareAdapter()
        prepareSpinners()
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

    // Prepares recyclerView adapter
    private fun prepareAdapter() {
        recyclerView = findViewById(R.id.spinnerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CategorySpinnerAdapter(listData, this) { position -> }
        addCategorySpinner()
    }

    private fun addCategorySpinner() {
        val spinner = Spinner(this)
        val s = SpinnerModel()
        s.spinner = spinner
        listData.add(s)
    }

    // Prepares Spinners' Adapters
    private fun prepareSpinners() {
        //val gameModeSpinner: Spinner = findViewById(R.id.gameModeSpinner)
        roundNumSpinner = findViewById(R.id.roundNumSpinner)
        categoryNumSpinner = findViewById(R.id.categoryNumSpinner)
        //createSpinner(gameModeSpinner, R.array.gamemode)
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

    private fun createGame() {
        gameId = myRef.child("Games").push().key.toString()
        var gameRef = myRef.child("Games").child(gameId)
        gameRef.child("Game_flag").setValue(false)
        gameRef.child("Players").child(myNick).setValue(true)
        for(i in 0..roundNumSpinner.selectedItemPosition)
            gameRef.child("Rounds").child((i+1).toString()).setValue(false)
        gameRef.child("Settings").child("Rounds_num").setValue(roundNumSpinner.selectedItemPosition+1)
        //for(i in 0..categoryNumSpinner.selectedItemPosition) {
        //    listData[i].spinner?.selectedItem.toString()
        //}

        // TODO Trzeba wyciągnąć z tych spinnerów wybrane kategorie
    }

    // Button takes you to a room activity
    fun confirm(view: View) {
        //createGame()
        val i = Intent(this, RoomActivity::class.java)
        i.putExtra("isHost", true)
        i.putExtra("gameId", gameId)
        startActivity(i)
    }
}