package com.example.pastwa_miasta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.pastwa_miasta.databinding.ActivityMainBinding
import com.example.pastwa_miasta.databinding.ActivityServerConnectionBinding
import com.example.pastwa_miasta.waiting_room.RoomActivity
import java.net.PortUnreachableException

class ServerConnectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServerConnectionBinding
    private lateinit var port: String
    private lateinit var hostIp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConnectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    // Button connects client to server
    fun connectToSever(view: View) {
        if (checkHostIp()  && checkPort()) {
            //if conected to server properly
            goToWaitingRoom()
        }
    }

    fun goToWaitingRoom() {
        val i = Intent(this, RoomActivity::class.java)
        startActivity(i)
    }

    fun checkPort(): Boolean {
        var text = binding.textInputPort.text
        if (text.toString().matches("-?\\d+(\\.\\d+)?".toRegex())) {
            port = text.toString()
            return true
        }
        else {
            binding.textInputPort.text?.clear()
            return false
        }
    }

    fun checkHostIp(): Boolean {
        var text = binding.textInputHostIp.text
        if (Patterns.IP_ADDRESS.matcher(text).matches()) {
            hostIp = text.toString()
            return true
        }
        else {
            binding.textInputHostIp.text?.clear()
            return false
        }
    }


}