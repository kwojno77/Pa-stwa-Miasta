package com.example.pastwa_miasta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.pastwa_miasta.create_game.CreateGameActivity
import com.example.pastwa_miasta.invitations.InvitationsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth


class MainMenuActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var appVersion: String? = null
    private lateinit var menuJoinButton: Button
    private lateinit var profileButton: FloatingActionButton
    private lateinit var menuCreateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mAuth = FirebaseAuth.getInstance()

        appVersion = intent.getStringExtra("version")

        menuJoinButton = findViewById(R.id.menuJoinButton)
        menuCreateButton = findViewById(R.id.menuCreateButton)
        profileButton = findViewById(R.id.profile)

        verifyVersion()
    }

    private fun verifyVersion() {
        if(!appVersion?.startsWith(BuildConfig.VERSION_NAME[0])!!) {
            menuJoinButton.setOnClickListener { toast() }
            profileButton.setOnClickListener { toast() }
            menuCreateButton.setOnClickListener { link() }
            menuCreateButton.text = "Pobierz nową wersję"
        } else {
            menuJoinButton.setOnClickListener { joinGame() }
            profileButton.setOnClickListener { viewProfile() }
            menuCreateButton.setOnClickListener { createGame() }
        }
    }

    private fun link() {
        val uri = Uri.parse("https://1drv.ms/u/s!Akcb49Tsg5dGgbpOv-E4ZEIp4bjD1w?e=C9CEDN")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun toast() {
        Toast.makeText(baseContext, "Zaktualizuj do wersji $appVersion, żeby zagrać", Toast.LENGTH_SHORT).show()
    }

    // Button takes you to a game creating activity
    private fun createGame() {
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