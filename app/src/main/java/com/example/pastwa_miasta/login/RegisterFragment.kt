package com.example.pastwa_miasta.login

import android.app.Activity
import com.example.pastwa_miasta.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var loginView: EditText
    private lateinit var passwordView: EditText
    private lateinit var repeatedPasswordView: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)
        mAuth = FirebaseAuth.getInstance()
        db = Firebase.database("https://panstwamiasta-5c811-default-rtdb.europe-west1.firebasedatabase.app/")

        loginView = view.findViewById(R.id.loginView)
        passwordView = view.findViewById(R.id.passwordView)
        repeatedPasswordView = view.findViewById(R.id.passwordRepeatView)

        val registerButton = view.findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            registerToFirebase(loginView.text.toString(),
                               passwordView.text.toString(),
                               repeatedPasswordView.text.toString())
        }
        return view
    }

    private fun registerToFirebase(email: String, pass: String, pass2: String) {
        if(!validation(email, pass, pass2)) return
        mAuth!!.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(context as Activity) { task ->
            if (task.isSuccessful) {
                var currentUser = mAuth!!.currentUser
                if (currentUser != null) {
                    val nick = currentUser.email!!.split("@")[0]
                    db.reference.child("Users").child(nick).child("Uid").setValue(currentUser.uid)
                    clearForm()
                    Toast.makeText(context, "Utworzono nowe konto: $nick", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e("Firebase ", "Error: ", task.exception)
                loginView.error = "Taki użytkownik już istnieje"
            }
        }
    }

    private fun validation(email: String, pass: String, pass2: String): Boolean {
        val passAlert = validatePassword(pass, pass2)
        var isValid = true
        if(!validateEmail(email)) {
            loginView.error = "Błędny email!"
            isValid = false
        }
        if(passAlert.isEmpty())
        else {
            passwordView.error = passAlert
            repeatedPasswordView.error = passAlert
            isValid = false
        }
        return isValid
    }

    private fun clearForm() {
        loginView.text.clear()
        passwordView.text.clear()
        repeatedPasswordView.text.clear()
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