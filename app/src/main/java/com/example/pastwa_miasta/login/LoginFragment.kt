package com.example.pastwa_miasta.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.pastwa_miasta.MainMenuActivity
import com.example.pastwa_miasta.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var passwordView: EditText
    private lateinit var registerEntry: Button
    private lateinit var resetPassButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        mAuth = FirebaseAuth.getInstance()
        val loginView = view.findViewById<EditText>(R.id.loginView)
        passwordView = view.findViewById(R.id.passwordView)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        progressBar = view.findViewById(R.id.loginProgressBar)
        progressBar.visibility = View.INVISIBLE

        registerEntry = view.findViewById(R.id.registerButton)
        registerEntry.setOnClickListener { startRegistration() }

        resetPassButton = view.findViewById(R.id.resetPassButton)

        loginView.addTextChangedListener {
            loginButton.isEnabled = passwordView.text.length > 5 && loginView.text.contains(Regex(".+@.+"))
        }

        passwordView.addTextChangedListener {
            loginButton.isEnabled = passwordView.text.length > 5 && loginView.text.contains(Regex(".+@.+"))
        }

        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            loginToFirebase(loginView.text.toString(), passwordView.text.toString())
        }

        resetPassButton.setOnClickListener { showResetDialog() }

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
                    if (currentUser != null ) {//&& currentUser.isEmailVerified) {
                        Toast.makeText(context, "Witaj ${currentUser.displayName}", Toast.LENGTH_SHORT).show()
                        loadMenu()
                    } else {
                        mAuth.signOut()
                        Toast.makeText(context, "Musisz najpierw zweryfikować swój email!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    passwordView.error = "Błędny login lub hasło"
                }
                progressBar.visibility = View.INVISIBLE
            }}

    private fun loadMenu() {
        val i = Intent(activity, MainMenuActivity::class.java)
        startActivity(i)
        activity!!.finish()
    }

    private fun startRegistration() {
        val i = Intent(context, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(mAuth.currentUser != null ) {//&& mAuth.currentUser!!.isEmailVerified) {
            loadMenu()
        }
    }

    private fun showResetDialog() {
        var builder = AlertDialog.Builder(context, R.style.AlertDialog_AppCompat_Light)
        var customLayout = layoutInflater.inflate(R.layout.dialog_password_reset, null)
        builder.setView(customLayout)
        builder.setTitle("Reset hasła")

        builder.setPositiveButton("Wyślij email resetujący") { dialog, id ->
            var text = customLayout.findViewById<EditText>(R.id.emailEntryDialog)
            sendResetEmail(text!!.text.toString())
        }

        builder.create().show()


    }

    private fun sendResetEmail(text: String) {
        mAuth.sendPasswordResetEmail(text)
    }
}