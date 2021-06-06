package com.example.pastwa_miasta.waiting_room

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.Player
import com.example.pastwa_miasta.R
import com.example.pastwa_miasta.ViewProfileActivity
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.GameActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RoomActivity : AppCompatActivity(), IRecyclerViewClick {
    private lateinit var joinedRecyclerView: RecyclerView
    private lateinit var invitedRecyclerView: RecyclerView
    private lateinit var joinedPlayersList: ArrayList<Player>
    private lateinit var invitedPlayersList: ArrayList<Player>
    private lateinit var playerCounterView: TextView
    private lateinit var playerNickEditText: EditText
    private var backPressed: Long = 0

    private var isHost: Boolean = false
    private lateinit var myNick: String
    private lateinit var gameId: String

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        checkUser()
        gameId = intent.getStringExtra("gameId").toString()
        isHost = intent.getBooleanExtra("isHost", false)
        gameRef = db.reference.child("Games").child(gameId!!)
        joinedPlayersList = ArrayList()
        invitedPlayersList = ArrayList()

        findViewById<Button>(R.id.startGameButton).setOnClickListener {
            startGame()
        }

        findViewById<Button>(R.id.inviteButton).setOnClickListener {
            checkIfPlayerAlreadyJoined()
        }

        setViews()
        listenForJoiningPlayers()
        listenForInvitedPlayers()
        listenForGameStart()
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

    private fun setViews() {
        findViewById<Button>(R.id.startGameButton).isVisible = isHost
        playerNickEditText = findViewById(R.id.playersNicksToInviteEditText)
        invitedRecyclerView = findViewById(R.id.recyclerViewRoomInvited)
        invitedRecyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter2 = RoomAdapter(false, invitedPlayersList, this)
        invitedRecyclerView.adapter = customAdapter2
        invitedRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(invitedRecyclerView)
        
        joinedRecyclerView = findViewById(R.id.recyclerViewRoomJoined)
        joinedRecyclerView.layoutManager = LinearLayoutManager(this)
        val customAdapter = RoomAdapter(true, joinedPlayersList, this)
        joinedRecyclerView.adapter = customAdapter
        joinedRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        playerCounterView = findViewById(R.id.playerCounterLabel)
        playerCounterView.text = "${joinedPlayersList.size} graczy"
    }

    private var itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val player = invitedPlayersList[viewHolder.adapterPosition]
                gameRef.child("Invited").child(player.name).removeValue()
                db.reference.child("Users").child(player.name).child("Requests").child(myNick).removeValue()
                invitedRecyclerView.adapter!!.notifyDataSetChanged()
            }
        }

    private fun listenForJoiningPlayers() {
        gameRef.child("Players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                joinedPlayersList.clear()
                dataSnapshot.children.forEach {
                    joinedPlayersList.add(Player(it.key.toString()))
                }
                refresh()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun listenForInvitedPlayers() {
        gameRef.child("Invited").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                invitedPlayersList.clear()
                dataSnapshot.children.forEach {
                    invitedPlayersList.add(Player(it.key.toString()))
                }
                refresh()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkIfPlayerAlreadyJoined() {
        val nick = playerNickEditText.text.toString()
        gameRef.child("Players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(nick).exists()) {
                    playerNickEditText.error = "Ten gracz już dołączył!"
                } else {
                    checkIfPlayerAlreadyInvited(nick)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkIfPlayerAlreadyInvited(nick: String) {
        gameRef.child("Invited").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(nick).exists()) {
                    playerNickEditText.error = "Ten gracz już został zaproszony!"
                } else {
                    checkIfPlayerExists(nick)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun inviteInDatabase(nick: String) {
        db.reference.child("Users").child(nick).child("Requests").child(myNick).setValue(gameId)
        gameRef.child("Invited").child(nick).setValue(gameId)
    }

    private fun checkIfPlayerExists(nick: String) {
        db.reference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(nick).exists()) {
                    inviteInDatabase(nick)
                } else {
                    playerNickEditText.error = "Gracz o takim nicku nie istnieje!"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase ", "Error: ", error.toException())
            }
        })
    }

    private fun listenForGameStart() {
        if(isHost) return
        gameRef.child("Game_flag")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value == true)
                    startGame()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refresh() {
        playerCounterView.text = "${joinedPlayersList.size} graczy"
        joinedRecyclerView.adapter?.notifyDataSetChanged()
        invitedRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putParcelableArrayList("joinedPlayersList", java.util.ArrayList(joinedPlayersList))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredAllPictures = savedInstanceState.getParcelableArrayList<Player>("joinedPlayersList")
        if(restoredAllPictures != null) {
            joinedPlayersList = restoredAllPictures
            (joinedRecyclerView.adapter as RoomAdapter).players = restoredAllPictures
        }
        refresh()
    }

    fun startGame() {
        if(isHost) cancelAllInvites()
        gameRef.child("Game_flag").setValue(true)
        db.reference.child("Games").child(gameId).child("CurrentRound").setValue(1)
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("gameId", gameId)
        startActivity(i)
        finish()
    }

    private fun cancelAllInvites() {
        gameRef.child("Invited")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                   dataSnapshot.children.forEach {
                       db.reference.child("Users").child(it.key.toString())
                           .child("Requests").child(myNick).removeValue()
                   }
                    dataSnapshot.ref.removeValue()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase ", "Error: ", error.toException())
                }
            })
    }

    private fun viewProfile(nick: String) {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", nick)
        startActivity(i)
    }

    override fun onJoinedAvatarClicked(pos: Int) {
        viewProfile(joinedPlayersList[pos].name)
    }

    override fun onInvitedAvatarClicked(pos: Int) {
        viewProfile(invitedPlayersList[pos].name)
    }

    override fun onBackPressed() {
        if (backPressed + 1000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                baseContext,
                "Jeśli wyjdziesz to już nie będziesz mógł dołączyć, czy na pewno tego chcesz?", Toast.LENGTH_SHORT
            )
                .show()
        }
        backPressed = System.currentTimeMillis()
    }
}