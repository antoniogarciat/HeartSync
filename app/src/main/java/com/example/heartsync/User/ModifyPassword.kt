package com.example.heartsync.User

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R

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
            val currentPasswordd = currentPasswordEditText.text.toString()
            val savedPassword = sharedPref.getString("USER_PASSWORD", "default")

            Log.d("ModifyPassword", "Current password (input): $currentPasswordd")
            Log.d("ModifyPassword", "Saved password (sharedPref): $savedPassword")

            if (currentPassword != sharedPref.getString("USER_PASSWORD", "")) {

                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Guardamos la nueva contraseña
            with(sharedPref.edit()) {
                putString("USER_PASSWORD", newPassword)
                apply()
            }

            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
