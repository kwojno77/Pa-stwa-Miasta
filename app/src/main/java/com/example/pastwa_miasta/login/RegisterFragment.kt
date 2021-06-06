package com.example.pastwa_miasta.login

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.example.pastwa_miasta.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var nickView: EditText
    private lateinit var loginView: EditText
    private lateinit var passwordView: EditText
    private lateinit var repeatedPasswordView: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)
        mAuth = FirebaseAuth.getInstance()
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        nickView = view.findViewById(R.id.nickView)
        loginView = view.findViewById(R.id.loginView)
        passwordView = view.findViewById(R.id.passwordView)
        repeatedPasswordView = view.findViewById(R.id.passwordRepeatView)
        progressBar = view.findViewById(R.id.registerProgressBar)
        progressBar.visibility = View.INVISIBLE

        val registerButton = view.findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            register(loginView.text.toString(),
                     passwordView.text.toString(),
                     repeatedPasswordView.text.toString(),
                     nickView.text.toString())
        }
        return view
    }

    private fun register(email: String, pass: String, pass2: String, nick: String) {
        if(!validation(email, pass, pass2, nick)) return
        db.reference.child("Users").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(nick).exists()) {
                    nickView.error = "Gracz o takim nicku już istnieje!"
                } else {
                    registerToFirebase(email, pass, nick)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase ", "Error: ", error.toException())
            }
        })
    }

    private fun registerToFirebase(email: String, pass: String, nick: String) {
        progressBar.visibility = View.VISIBLE
        mAuth!!.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(context as Activity) { task ->
            if (task.isSuccessful) {
                val currentUser = mAuth!!.currentUser
                if (currentUser != null) {
                    sendVerificationMail(currentUser)
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(nick).build()
                    currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Firebase", "User profile updated")
                            }
                        }
                    db.reference.child("Users").child(nick).child("Uid").setValue(currentUser.uid)
                    db.reference.child("Users").child(nick).child("Email").setValue(currentUser.email)
                    db.reference.child("Users").child(nick).child("Stats").child("WonGames").setValue(0)
                    db.reference.child("Users").child(nick).child("Stats").child("Points").setValue(0)
                    db.reference.child("Users").child(nick).child("Friends").setValue(true)
                    clearForm()
                }
            } else {
                Log.e("Firebase ", "Error: ", task.exception)
                if(task.exception is FirebaseAuthInvalidCredentialsException) {
                    loginView.error = "Email jest w złym formacie!"
                } else {
                    loginView.error = task.exception?.message
                }
            }
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun sendVerificationMail(currentUser: FirebaseUser) {
        currentUser.sendEmailVerification().addOnCompleteListener(context as Activity) { task ->
            if(task.isSuccessful) {
                Toast.makeText(context, "Utworzono nowe konto: ${currentUser.displayName}\nZweryfikuj maila zanim się zalogujesz", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validation(email: String, pass: String, pass2: String, nick: String): Boolean {
        val passAlert = validatePassword(pass, pass2)
        var isValid = true
        if(!validateEmail(email)) {
            loginView.error = "Błędny email!"
            isValid = false
        }
        if(passAlert.isNotEmpty()) {
            passwordView.error = passAlert
            repeatedPasswordView.error = passAlert
            isValid = false
        }
        if(!validateNick(nick)) {
            nickView.error = "Nick powinien mieć co najmniej 6 znaków!"
            isValid = false
        }
        return isValid
    }

    private fun clearForm() {
        loginView.text.clear()
        passwordView.text.clear()
        repeatedPasswordView.text.clear()
        nickView.text.clear()
    }

    private fun validateNick(nick: String): Boolean {
        return nick.length > 1 // To change
    }

    private fun validateEmail(email: String): Boolean {
        return email.contains(Regex(".+@.+"))
    }

    private fun validatePassword(pass: String, pass2: String): String {
        if(pass != pass2) return "Hasła powinny być takie same!"
        if(pass.length < 6) return "Hasło powinno mieć przynajmniej 6 znaków!"
        if(pass == pass.toLowerCase()) return "Hasło powinno mieć przynajmniej jedną dużą literę!"
        if(pass == pass.toLowerCase()) return "Hasło powinno mieć przynajmniej jedną małą literę!"
        if(pass.isDigitsOnly()) return "Hasło powinno mieć przynajmniej dwie litery!"
        return ""
    }
}