package com.example.natal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback

class FirstActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val name = intent.getStringExtra("EXTRA_MESSAGE1")
        val password = intent.getStringExtra("EXTRA_MESSAGE2")

        val horoscopeButton = findViewById<Button>(R.id.button2)
        val natalButton = findViewById<Button>(R.id.button1)
        val compatibility = findViewById<Button>(R.id.button3)
        val exitButton = findViewById<ImageButton>(R.id.buttonExit)
        val communityButton = findViewById<Button>(R.id.communityButton)

        communityButton.setOnClickListener {
            val intent = Intent(this@FirstActivity, CommunityActivity::class.java)
            intent.putExtra("EXTRA_MESSAGE1", name)
            intent.putExtra("EXTRA_MESSAGE2", password)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        horoscopeButton.setOnClickListener {
            val intent = Intent(this@FirstActivity, HoroscopeActivity::class.java)
            intent.putExtra("EXTRA_MESSAGE1", name)
            intent.putExtra("EXTRA_MESSAGE2", password)
            startActivity(intent)
        }

        natalButton.setOnClickListener {
            val intent2 = Intent(this@FirstActivity, NatalActivity::class.java)
            intent2.putExtra("EXTRA_MESSAGE1", name)
            intent2.putExtra("EXTRA_MESSAGE2", password)
            startActivity(intent2)
        }

        compatibility.setOnClickListener {
            val intent3 = Intent(this@FirstActivity, CompatibilityActivity::class.java)
            intent3.putExtra("EXTRA_MESSAGE1", name)
            intent3.putExtra("EXTRA_MESSAGE2", password)
            startActivity(intent3)
        }

        // Новый способ обработки кнопки "Назад"
        setupBackPressedHandler()
    }

    private fun setupBackPressedHandler() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход из приложения")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { dialog, which ->
                finishAffinity()
            }
            .setNegativeButton("Отмена") { dialog, which ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}