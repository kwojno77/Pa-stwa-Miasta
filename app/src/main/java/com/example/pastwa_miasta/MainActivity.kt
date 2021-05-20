package com.example.pastwa_miasta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.pastwa_miasta.databinding.ActivityMainBinding

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
}