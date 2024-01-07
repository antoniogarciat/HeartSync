package com.example.heartsync.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R
import android.util.Patterns
import android.widget.TextView
import com.example.dashboard.pantallas.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserSignup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val signupButton = findViewById<Button>(R.id.signupButton)
        val signupName = findViewById<EditText>(R.id.signupName)
        val signupEmail = findViewById<EditText>(R.id.signupEmail)
        val signupPassword = findViewById<EditText>(R.id.signupPassword)
        val signupAge = findViewById<EditText>(R.id.signupAge)
        val signupGenderMale = findViewById<RadioButton>(R.id.radio_male)
        val signupGenderFemale = findViewById<RadioButton>(R.id.radio_female)
        val loginButton = findViewById<TextView>(R.id.textViewLogIn)

        loginButton.setOnClickListener {
            val intent = Intent(this, UserLogin::class.java)
            startActivity(intent)
        }


        signupButton.setOnClickListener {
            val name = signupName.text.toString().trim()
            val email = signupEmail.text.toString().trim()
            val password = signupPassword.text.toString().trim()
            val ageString = signupAge.text.toString().trim()
            val age = ageString.toIntOrNull() ?: -1
            val gender = when {
                signupGenderMale.isChecked -> "Male"
                signupGenderFemale.isChecked -> "Female"
                else -> ""
            }

            if (validarCampos(name, email, password, age, gender)) {
                registrarUsuario(name, email, password, age, gender)
            }
        }
    }

    private fun validarCampos(name: String, email: String, password: String, age: Int, gender: String): Boolean {
        return containsOnlyLetters(name) && isValidEmail(email) && isValidPassword(password) && isValidAge(age) && gender.isNotBlank()
    }

    private fun registrarUsuario(name: String, email: String, password: String, age: Int, gender: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    subirInformacionPerfil(user, name, age, gender,email)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun guardarDatosSharedPreferences(name: String, email: String, age: Int, gender: String) {
        val sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_NAME", name)
            putString("USER_EMAIL", email)
            putInt("USER_AGE", age)
            putString("USER_GENDER", gender)
            apply()
        }
    }

    private fun subirInformacionPerfil(user: FirebaseUser?, name: String, age: Int, gender: String, email: String) {
        val userProfile = UserProfile(name, age, gender,email)
        val userProfileRef = Firebase.database.getReference("users/${user?.uid}/profileInfo")
        userProfileRef.setValue(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload profile info.", Toast.LENGTH_LONG).show()
            }
    }

    private fun containsOnlyLetters(input: String): Boolean {
        return input.all { it.isLetter() }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= minLength && hasUpperCase && hasDigit
    }

    private fun isValidAge(age: Int): Boolean {
        return age in 5..120
    }



}

