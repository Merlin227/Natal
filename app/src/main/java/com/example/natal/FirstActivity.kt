package com.example.natal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class FirstActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val name = intent.getStringExtra("EXTRA_MESSAGE1")
        val password = intent.getStringExtra("EXTRA_MESSAGE2")

        val horoscopeButton = findViewById<Button>(R.id.button2)
        val natalButton = findViewById<Button>(R.id.button1)

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
    }
}