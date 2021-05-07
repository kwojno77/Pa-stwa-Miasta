package com.example.pastwa_miasta

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
        //Greeting toast
        Toast.makeText(this, "Witamy w ${binding.gameTitle.text}", Toast.LENGTH_SHORT).show()
    }

    //button to confirm nickname and run next activity
    fun confirm(view: View) {
        if (binding.inputUsername.text.length >= 3)
            Toast.makeText(this, "Super", Toast.LENGTH_SHORT).show()
    }
}