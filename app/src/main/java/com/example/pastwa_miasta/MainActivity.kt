package com.example.pastwa_miasta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pastwa_miasta.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Greeting toast
        Toast.makeText(this, "Witamy w ${binding.gameTitle.text}", Toast.LENGTH_SHORT).show()

        findViewById<FloatingActionButton>(R.id.profile).setOnClickListener {
            viewProfile()
        }
    }

    // Button to confirm nickname and run main menu activity
    fun confirm(view: View) {
        if (binding.inputUsername.text.length >= 3) {
            val i = Intent(this, MainMenuActivity::class.java)
            startActivity(i)
        }
    }

    // TEMPORARY
    fun check(view: View) {
        val i = Intent(this, MainMenuActivity::class.java)
        startActivity(i)
    }

    private fun viewProfile() {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", "null")
        startActivity(i)
    }
}