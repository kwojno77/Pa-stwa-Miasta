package com.example.pastwa_miasta

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pastwa_miasta.create_game.CreateGameActivity
import com.example.pastwa_miasta.invitations.InvitationsActivity

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
        val i = Intent(this, InvitationsActivity::class.java)
        startActivity(i)
    }
}