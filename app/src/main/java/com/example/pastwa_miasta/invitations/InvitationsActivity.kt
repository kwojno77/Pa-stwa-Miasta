package com.example.pastwa_miasta.invitations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.waiting_room.RoomActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class InvitationsActivity: AppCompatActivity(), IButtonClick {
    private lateinit var recyclerView: RecyclerView
    private lateinit var invitationsList: ArrayList<InvitationInfo>
    private lateinit var myNick: String

    private lateinit var db: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var myRequests: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = db.reference
        checkUser()
        myRequests = db.getReference("Users").child(myNick).child("Requests")
        invitationsList = ArrayList()
        recyclerViewInit()
        requestsListenerInit()
    }

    private fun requestsListenerInit() {
        myRequests.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                invitationsList.clear()
                dataSnapshot.children.forEach {
                    invitationsList.add(InvitationInfo(it.key.toString(), it.value.toString()))
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Błąd przy słuchaniu zaproszeń: ${error.message}")
            }
        })
    }

    private fun recyclerViewInit() {
        recyclerView = findViewById(R.id.recyclerViewInv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = InvitationsAdapter(invitationsList, this)
        recyclerView.adapter = customAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun checkUser() {
        val currUser = FirebaseAuth.getInstance().currentUser
        if (currUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            myNick = currUser.displayName.toString()
        }
    }
    
    override fun onAcceptClick(pos: Int) {
        val gameId = invitationsList[pos].gameId
        myRef.child("Users").child(myNick).child("Requests")
            .child(invitationsList[pos].gameOwnerNick).removeValue()
        myRef.child("Games").child(gameId).child("Invited")
            .child(myNick).removeValue()
        myRef.child("Games").child(gameId).child("Players")
            .child(myNick).child("Points").setValue(0)
        var i = Intent(this, RoomActivity::class.java)
        i.putExtra("gameId", gameId)
        startActivity(i)
    }

    override fun onDeclineClick(pos: Int) {
        val gameId = invitationsList[pos].gameId
        myRef.child("Users").child(myNick).child("Requests")
            .child(invitationsList[pos].gameOwnerNick).removeValue()
        myRef.child("Games").child(gameId).child("Invited")
            .child(myNick).removeValue()
    }
}