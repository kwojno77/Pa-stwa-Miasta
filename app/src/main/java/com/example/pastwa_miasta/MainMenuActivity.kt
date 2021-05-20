package com.example.pastwa_miasta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.pastwa_miasta.waiting_room.RoomActivity
import com.example.pastwa_miasta.ServerConnectionActivity

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    // Button takes you to a game creating activity
    fun createGame(view: View) {
        val i = Intent(this, CreateGameActivity::class.java)
        startActivity(i)
    }

    // Button takes you to X activity
    fun joinGame(view: View) {
        val i = Intent(this, ServerConnectionActivity::class.java)
        startActivity(i)
    }
}