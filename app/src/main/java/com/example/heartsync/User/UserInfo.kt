package com.example.heartsync.User

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val useremailTextView = findViewById<TextView>(R.id.useremailTextView)
        val userageTextView = findViewById<TextView>(R.id.userageTextView)
        val usergenderTextView = findViewById<TextView>(R.id.usergenderTextView)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            cargarDatosUsuario(uid, usernameTextView, useremailTextView, userageTextView, usergenderTextView)
        } else {
            // Manejar el caso de que no hay un usuario autenticado
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosUsuario(uid: String, usernameTextView: TextView, useremailTextView: TextView, userageTextView: TextView, usergenderTextView: TextView) {
        val databaseReference = Firebase.database.getReference("users/$uid/profileInfo")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    usernameTextView.text = userProfile.name
                    userageTextView.text = getString(R.string.label_age, userProfile.age.toString())
                    usergenderTextView.text = getString(R.string.label_gender, userProfile.gender)
                    useremailTextView.text = getString(R.string.label_email, userProfile.email)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserInfo, "Failed to retrieve user data.", Toast.LENGTH_LONG).show()
            }
        })
    }

}
