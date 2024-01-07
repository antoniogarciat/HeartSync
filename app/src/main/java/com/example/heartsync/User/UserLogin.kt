package com.example.heartsync.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.HeartRate.HeartRateData
import com.example.dashboard.pantallas.MainActivity
import com.example.heartsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserLogin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.loginEmail)
        val passwordEditText = findViewById<EditText>(R.id.loginPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                iniciarSesion(email, password)
            }
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        recuperarDatosUsuario(user.uid)
                        recuperarDatosHeartRate(user.uid)
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun guardarDatosSharedPreferences(name: String, age: Int, gender: String) {
        val sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_NAME", name)
            putInt("USER_AGE", age)
            putString("USER_GENDER", gender)
            apply()
        }
    }

    private fun recuperarDatosUsuario(uid: String) {
        val databaseReference = Firebase.database.getReference("users/$uid/profileInfo")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    guardarDatosSharedPreferences(userProfile.name, userProfile.age, userProfile.gender)
                    startActivity(Intent(this@UserLogin, MainActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserLogin, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun recuperarDatosHeartRate(uid: String) {
        val databaseReference = Firebase.database.getReference("users/$uid/heartRateData")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val heartRateList = mutableListOf<HeartRateData>()
                snapshot.children.forEach {
                    val heartRateData = it.getValue(HeartRateData::class.java)
                    heartRateData?.let { data ->
                        heartRateList.add(data)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserLogin, "Failed to retrieve heart rate data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


}

