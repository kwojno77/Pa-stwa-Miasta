package com.example.pastwa_miasta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.pastwa_miasta.create_game.CreateGameActivity
import com.example.pastwa_miasta.invitations.InvitationsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MainMenuActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        mAuth = FirebaseAuth.getInstance();

        findViewById<Button>(R.id.menuJoinButton).setOnClickListener {
            joinGame()
        }

        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }
    }

    // Button takes you to a game creating activity
    fun createGame(view: View) {
        val i = Intent(this, CreateGameActivity::class.java)
        startActivity(i)
    }

    // Button takes you to X activity
    private fun joinGame() {
        val i = Intent(this, InvitationsActivity::class.java)
        startActivity(i)
    }

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }
}