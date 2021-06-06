package com.example.pastwa_miasta.waiting_room

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendsList: AppCompatActivity(), FriendsListRecyclerViewClick {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    lateinit var currentUser: String
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsList: ArrayList<Player>
    private lateinit var friendsAdapter: FriendsListAdapter
    private lateinit var gameRef: DatabaseReference
    private lateinit var gameId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)
        var param = intent.getStringExtra("user").toString()
        friendsRecyclerView = findViewById(R.id.recyclerViewFriends)
        mAuth = FirebaseAuth.getInstance();
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        gameId = intent.getStringExtra("gameId").toString()
        gameRef = db.reference.child("Games").child(gameId!!)
        currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        friendsList = ArrayList()
        friendsAdapter = FriendsListAdapter(friendsList, this)
        friendsRecyclerView.adapter = friendsAdapter
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        getFriendsList()
    }

    override fun onButtonClicked(pos: Int, button: Button) {
        inviteInDatabase(friendsList[pos].name)
        button.isClickable = false
        button.setBackgroundColor(Color.GRAY)
    }
    private fun checkIfPlayerAlreadyJoined(nick: String) {
        gameRef.child("Players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.child(nick).exists()) {
                    checkIfPlayerAlreadyInvited(nick)
                }
                else {
                    println("player już dołączył")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkIfPlayerAlreadyInvited(nick: String) {
        gameRef.child("Invited").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.child(nick).exists()) {
                    friendsList.add(Player(nick))
                    friendsRecyclerView.adapter!!.notifyDataSetChanged()
                }
                else {
                    println("player już zaproszony")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun inviteInDatabase(nick: String) {
        println("inviting")
        db.reference.child("Users").child(nick).child("Requests").child(currentUser).setValue(gameId)
        gameRef.child("Invited").child(nick).setValue(gameId)
    }

    override fun onAvatarClicked(pos: Int) {
        viewProfile(friendsList[pos].name)
    }

    private fun viewProfile(nick: String) {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", nick)
        startActivity(i)
    }

    private fun getFriendsList() {
        println("getFriendsList")
        db.reference.child("Users").child(currentUser).child("Friends")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    friendsList.clear()
                    dataSnapshot.children.forEach {
                        checkIfPlayerAlreadyJoined(it.key.toString())
                        println(it.key.toString())
                    }
                    friendsRecyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}