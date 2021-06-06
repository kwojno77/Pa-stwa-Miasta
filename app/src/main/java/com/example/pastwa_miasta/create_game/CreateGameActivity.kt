package com.example.pastwa_miasta.create_game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.waiting_room.RoomActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random


class CreateGameActivity : AppCompatActivity() {

    private val CATEGORIES_MAX: Int = 6

    private lateinit var db: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private var categorySpinners : MutableList<Spinner> = ArrayList()
    private lateinit var roundNumSpinner: Spinner
    private lateinit var categoryNumSpinner: Spinner

    private lateinit var myNick: String
    private lateinit var gameId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)

        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }
        findViewById<Button>(R.id.createGameRollButton).setOnClickListener {
            rollCategories()
        }

        //var gameId = "1" // TYMCZASOWE
        //db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        //gameRef = db.reference.child("Games").child(gameId!!)

        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = db.reference
        checkUser()
        prepareSpinners()
        setDefaultOptions()
    }

    private fun setDefaultOptions() {
        roundNumSpinner.setSelection(2)
        categoryNumSpinner.setSelection(3)
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

    // Prepares Spinners' Adapters
    private fun prepareSpinners() {
        roundNumSpinner = findViewById(R.id.roundNumSpinner)
        categoryNumSpinner = findViewById(R.id.categoryNumSpinner)
        createSpinner(roundNumSpinner, R.array.roundsNum)
        createSpinner(categoryNumSpinner, R.array.categoryNum)

        prepareCategorySpinners()

        categoryNumSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { return }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val categoriesNumber: Int = categoryNumSpinner.selectedItem.toString().toInt()
                for (i in 0 until categoriesNumber) {
                    categorySpinners[i].visibility = View.VISIBLE
                }
                for (i in categoriesNumber until CATEGORIES_MAX) {
                    categorySpinners[i].visibility = View.INVISIBLE
                }
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

    private fun createGame(): Boolean {
        if(isRepeated(categoryNumSpinner.selectedItem.toString().toInt())) {
            Toast.makeText(this, "Wybrane kategorie nie mogą się powtarzać", Toast.LENGTH_SHORT).show()
            return false
        }
        gameId = myRef.child("Games").push().key.toString()
        val gameRef = myRef.child("Games").child(gameId)
        gameRef.child("Game_flag").setValue(false)
        gameRef.child("Players").child(myNick).child("Points").setValue(0)
        for(i in 0..roundNumSpinner.selectedItemPosition)
            gameRef.child("Rounds").child((i+1).toString()).setValue(false)
        gameRef.child("Settings").child("Rounds_num").setValue(roundNumSpinner.selectedItemPosition+1)
        val categories = ArrayList<String>()
        for(i in categorySpinners) {
            if (i.visibility == View.VISIBLE) {
                categories.add(i.selectedItem.toString())
            }
        }
        for(i in categories) {
            gameRef.child("Settings").child("Categories").child(i).setValue(true)
        }
        return true
    }

    private fun isRepeated(categoriesNum: Int): Boolean {
        for(i in 0 until categoriesNum) {
            for(j in i + 1 until categoriesNum) {
                if(categorySpinners[i].selectedItem.toString() == categorySpinners[j].selectedItem.toString()) {
                    return true
                }
            }
        }
        return false
    }

    // Button takes you to a room activity
    fun confirm(view: View) {
        if (!createGame()){
            return
        }
        val i = Intent(this, RoomActivity::class.java)
        i.putExtra("isHost", true)
        i.putExtra("gameId", gameId)
        startActivity(i)
        finish()
    }

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }

    private fun rollCategories() {
        val categoryArray: Array<String> = resources.getStringArray(R.array.categories)
        val indexList = mutableListOf<Int>()
        for (i in categoryArray.indices) {
            indexList.add(i)
        }
        for (cs in 0 until categorySpinners.size) {
            if (categorySpinners[cs].visibility == View.INVISIBLE) {
                break
            }
            val random = Random.nextInt(0, indexList.size)
            val index = indexList[random]
            indexList.removeAt(random)
            categorySpinners[cs].setSelection(index)
        }
    }

    private fun prepareCategorySpinners() {
        val myLayout = findViewById<LinearLayout>(R.id.main_linear)

        for (i in 0 until CATEGORIES_MAX) {
            val mySpinner = Spinner(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(200, 15, 200, 10)

            mySpinner.layoutParams = params
            myLayout.addView(mySpinner)
            createSpinner(mySpinner, R.array.categories)
            categorySpinners.add(mySpinner)

        }
        rollCategories()
        for (i in categorySpinners) {
            i.visibility = View.INVISIBLE
        }
    }

}
