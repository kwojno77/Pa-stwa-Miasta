package com.example.pastwa_miasta

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pastwa_miasta.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewProfileActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)
        var param = intent.getStringExtra("user").toString()

        mAuth = FirebaseAuth.getInstance();
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")


        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logout()
        }
        if(param == "null") {
            param = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            setData(param)
            findViewById<Button>(R.id.logoutButton).visibility = VISIBLE
        }
        else viewOtherPlayer(param)
    }

    fun logout() {
        mAuth.signOut();
        val i = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //nie działa tak jak powinno
        startActivity(i)
    }

    private fun viewOtherPlayer(user: String) {
        findViewById<Button>(R.id.logoutButton).visibility = INVISIBLE
        db.reference.child("Users").child(user)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var nicknameText = findViewById<TextView>(R.id.nicknameText)
                    nicknameText.text = user
                    var emailText = findViewById<TextView>(R.id.emailText)
                    emailText.text = dataSnapshot.child("Email").value as String
                    var points = findViewById<TextView>(R.id.points)
                    points.text = (dataSnapshot.child("Stats").child("Points").value as Long).toString()
                    var wonGames = findViewById<TextView>(R.id.wonGames)
                    wonGames.text = (dataSnapshot.child("Stats").child("WonGames").value as Long).toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setData(user: String) {
        var user = FirebaseAuth.getInstance().currentUser
        var nicknameText = findViewById<TextView>(R.id.nicknameText)
        if (user != null) {
            nicknameText.text = user.displayName.toString()
        }
        var emailText = findViewById<TextView>(R.id.emailText)
        if (user != null) {
            emailText.text = user.email.toString()
        }

        var points = findViewById<TextView>(R.id.points)
        var pointsData = 0
        var wonGames = findViewById<TextView>(R.id.wonGames)
        var wonGamesData = 0
        if (user != null) {
            println("user not null!!!!!!!!!!!!!!!!!")
            val postListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    println("updating data!!!!!!!!!")
                    pointsData = (dataSnapshot.child("Users").child(user.displayName.toString()).child("Stats").child("Points").value as Long).toInt()
                    wonGamesData = (dataSnapshot.child("Users").child(user.displayName.toString()).child("Stats").child("WonGames").value as Long).toInt()
                    points.setText(pointsData.toString())
                    wonGames.setText(wonGamesData.toString())
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            db.reference.addValueEventListener(postListener)
        }
    }
}