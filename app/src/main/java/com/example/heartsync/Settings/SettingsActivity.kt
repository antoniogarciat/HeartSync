package com.example.heartsync.Settings

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R
import com.example.heartsync.User.ModifyPassword
import com.example.heartsync.User.UserSignup
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val modifyUserInfo = findViewById<TextView>(R.id.modify_user_info)
        val aboutTheApp = findViewById<TextView>(R.id.app_info)
        val modifyPassword = findViewById<TextView>(R.id.modify_password)
        val deleteFile = findViewById<TextView>(R.id.delete_file)

        modifyUserInfo.setOnClickListener {
            val intent = Intent(this, UserSignup::class.java)
            intent.putExtra("edit_mode", true)
            startActivity(intent)
        }

        aboutTheApp.setOnClickListener {
            val intent = Intent(this, AppInfo::class.java)
            startActivity(intent)

        }

        modifyPassword.setOnClickListener {
            val intent = Intent(this, ModifyPassword::class.java)
            startActivity(intent)
        }

        deleteFile.setOnClickListener {
            deleteCsvFile()
        }

    }

    private fun deleteCsvFile() {
        val file = File(getExternalFilesDir(null), "HeartRateData.csv")
        if (file.exists()) {
            AlertDialog.Builder(this)
                .setTitle("Delete CSV file")
                .setMessage("Are you sure you want to delete the CSV file? This action cannot be undone and all data will be lost.")
                .setPositiveButton("Accept") { _, _ ->
                    file.delete()
                    Toast.makeText(this, "CSV file was deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(this, "CSV file could not be found", Toast.LENGTH_SHORT).show()
        }
    }

}
