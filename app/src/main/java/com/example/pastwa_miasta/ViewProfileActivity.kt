package com.example.pastwa_miasta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.login.LoginActivity
import com.example.pastwa_miasta.main_game.FriendsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewProfileActivity : AppCompatActivity(), friendsRecyclerViewClick {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    lateinit var currentUser: String
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsList: ArrayList<Player>
    private lateinit var playerNickEditText: EditText
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)
        var param = intent.getStringExtra("user").toString()
        friendsRecyclerView = findViewById(R.id.recyclerViewFriends)
        mAuth = FirebaseAuth.getInstance();
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")
        currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        friendsList = ArrayList()
        friendsAdapter = FriendsAdapter(friendsList, this)
        friendsRecyclerView.adapter = friendsAdapter
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        findViewById<Button>(R.id.logoutButton).visibility = VISIBLE
        if(param == "null" || param == currentUser) {
            findViewById<Button>(R.id.logoutButton).setOnClickListener {
                logout()
            }
            findViewById<Button>(R.id.inviteButton).setOnClickListener {
                var friend = findViewById<EditText>(R.id.playersNicksToInviteEditText).text.toString()
                checkIfPlayerExists(friend)
            }
            param = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            setData(param)
        }
        else viewOtherPlayer(param)
    }

    fun logout() {
        mAuth.signOut();
        val i = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i)
        finish()
    }

    private fun deleteFriend(user: String) {
        db.reference.child("Users").child(currentUser).child("Friends").child(user).removeValue()
        db.reference.child("Users").child(user).child("Friends").child(currentUser).removeValue()
        findViewById<Button>(R.id.logoutButton).text = "Dodaj"
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            addFriend(user)
        }
    }

    private fun addFriend(user: String) {
        db.reference.child("Users").child(currentUser).child("Friends").child(user).setValue(true)
        db.reference.child("Users").child(user).child("Friends").child(currentUser).setValue(true)
        findViewById<Button>(R.id.logoutButton).text = "Usuń"
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            deleteFriend(user)
        }
    }

    private fun viewOtherPlayer(user: String) {
        db.reference.child("Users").child(currentUser).child("Friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChild(user)) {
                    findViewById<Button>(R.id.logoutButton).text = "Dodaj"
                    findViewById<Button>(R.id.logoutButton).setOnClickListener {
                        addFriend(user)
                    }
                }
                else {
                    findViewById<Button>(R.id.logoutButton).text = "Usuń"
                    findViewById<Button>(R.id.logoutButton).setOnClickListener {
                        deleteFriend(user)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
        findViewById<EditText>(R.id.playersNicksToInviteEditText).visibility = VISIBLE
        playerNickEditText = findViewById(R.id.playersNicksToInviteEditText)
        findViewById<Button>(R.id.inviteButton).visibility = VISIBLE
        findViewById<TextView>(R.id.friendsLabel).visibility = VISIBLE
        findViewById<RecyclerView>(R.id.recyclerViewFriends).visibility = VISIBLE
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
            getFriendsList()
        }
    }

    override fun onAvatarClicked(pos: Int) {
        viewProfile(friendsList[pos].name)
    }

    private fun viewProfile(nick: String) {
        val i = Intent(this, ViewProfileActivity::class.java)
        i.putExtra("user", nick)
        startActivity(i)
    }

    private fun inviteFriend(friend: String) {
        db.reference.child("Users").child(currentUser).child("Friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.hasChild(friend)) {
                        db.reference.child("Users").child(currentUser).child("Friends").child(friend).setValue(true)
                        db.reference.child("Users").child(friend).child("Friends").child(currentUser).setValue(true)
                        }
                    else { playerNickEditText.error = "Ten gracz już jest Twoim znajomym!" }
                    }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getFriendsList() {
        db.reference.child("Users").child(currentUser).child("Friends")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    friendsList.clear()
                    dataSnapshot.children.forEach {
                        friendsList.add(Player(it.key.toString()))
                    }
                    friendsRecyclerView.adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkIfPlayerExists(nick: String) {
        db.reference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.child(nick).exists()) {
                        inviteFriend(nick)
                    } else {
                        playerNickEditText.error = "Gracz o takim nicku nie istnieje!"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase ", "Error: ", error.toException())
                }
            })
    }

    @Override
    override fun onResume() {
        super.onResume()
        getFriendsList()
    }
}