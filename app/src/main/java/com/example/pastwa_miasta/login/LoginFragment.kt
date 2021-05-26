package com.example.pastwa_miasta.login

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.pastwa_miasta.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var passwordView: EditText
    private lateinit var registerEntry: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        mAuth = FirebaseAuth.getInstance();
        val loginView = view.findViewById<EditText>(R.id.loginView)
        passwordView = view.findViewById(R.id.passwordView)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        registerEntry = view.findViewById(R.id.registerButton)
        registerEntry.setOnClickListener { startRegistration() }

        loginView.addTextChangedListener {
            loginButton.isEnabled = passwordView.text.length > 5 && loginView.text.contains(Regex(".+@.+"))
        }

        passwordView.addTextChangedListener {
            loginButton.isEnabled = passwordView.text.length > 5 && loginView.text.contains(Regex(".+@.+"))
        }

        loginButton.setOnClickListener {
            loginToFirebase(loginView.text.toString(), passwordView.text.toString())
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            registerEntry.visibility = View.INVISIBLE
        else
            registerEntry.visibility = View.VISIBLE
    }

    private fun loginToFirebase(login: String, pass: String) {
        mAuth.signInWithEmailAndPassword(login, pass)
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    var currentUser = mAuth!!.currentUser
                    if (currentUser != null) {
                        Toast.makeText(context, "Witaj ${login.split("@")[0]}", Toast.LENGTH_SHORT).show()
                        loadMenu()
                    }
                } else {
                    passwordView.error = "Błędny login lub hasło"
                }
            }}

    private fun loadMenu() {
        TODO("Not yet implemented")
    }

    private fun startRegistration() {
        val i = Intent(context, RegisterActivity::class.java)
        startActivity(i)
    }
}