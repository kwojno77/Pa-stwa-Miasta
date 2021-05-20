package com.example.pastwa_miasta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.pastwa_miasta.waiting_room.RoomActivity

class CreateGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        preapareSpinners()
    }

    // Prepares Spinners' Adapters
    private fun preapareSpinners() {
        val gameModeSpinner: Spinner = findViewById(R.id.gameModeSpinner)
        val roundNumSpinner: Spinner = findViewById(R.id.roundNumSpinner)
        val categoryNumSpinner: Spinner = findViewById(R.id.categoryNumSpinner)
        createSpinner(gameModeSpinner, R.array.gamemode)
        createSpinner(roundNumSpinner, R.array.roundsNum)
        createSpinner(categoryNumSpinner, R.array.categoryNum)
    }

    // Create a Spinner by getting Spinner object and a resource array
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
        startActivity(i)
    }
}