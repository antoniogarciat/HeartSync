package com.example.heartsync.User

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ModifyPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val currentPasswordEditText = findViewById<EditText>(R.id.currentPassword)
        val newPasswordEditText = findViewById<EditText>(R.id.newPassword)
        val confirmNewPasswordEditText = findViewById<EditText>(R.id.confirmNewPassword)
        val saveNewPasswordButton = findViewById<Button>(R.id.saveNewPasswordButton)

        val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)

        saveNewPasswordButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

            if (newPassword != confirmNewPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val email = firebaseUser?.email ?: ""

            // reautenticar usuario
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            firebaseUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // actualizamos valor contraseña en Firebase Authentication
                    firebaseUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            // actualizamos valor en SharedPreferences
                            with(sharedPref.edit()) {
                                putString("USER_PASSWORD", newPassword)
                                apply()
                            }
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
