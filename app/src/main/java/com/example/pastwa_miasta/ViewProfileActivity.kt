package com.example.pastwa_miasta

import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.Answer

class ViewProfileActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)
        val param = intent.getStringExtra("user").toString()

        mAuth = FirebaseAuth.getInstance();

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logout()
        }

        if (param.equals("null")) {
            setData()
            findViewById<Button>(R.id.logoutButton).visibility = VISIBLE
        }
    }

    fun logout() {
        mAuth.signOut();
        val i = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //nie działa tak jak powinno
        startActivity(i)
    }

    private fun setData() {
        var user = FirebaseAuth.getInstance().currentUser
        var nicknameText = findViewById<TextView>(R.id.nicknameText)
        if (user != null) {
            nicknameText.setText(user.displayName.toString())
        }
        var emailText = findViewById<TextView>(R.id.emailText)
        if (user != null) {
            emailText.setText(user.email.toString())
        }
    }
}