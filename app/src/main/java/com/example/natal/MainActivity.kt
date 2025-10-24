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

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val sendButtonLogin = findViewById<Button>(R.id.loginButton)
        val resultTextView = findViewById<TextView>(R.id.textView4)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiServices::class.java)

        sendButtonLogin.setOnClickListener {
            val name = editTextName.text.toString()
            val password = editTextPassword.text.toString()

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = UserData(
                Name = name,
                Password = password
            )

            apiService.sendData(userData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!


                        if (result.status == "True") {
                            resultTextView.text = "Успешный вход!"
                            Toast.makeText(this@MainActivity, "Успешный вход!", Toast.LENGTH_SHORT).show()

                            editTextName.text.clear()
                            editTextPassword.text.clear()

                            val intent = Intent(this@MainActivity, FirstActivity::class.java)
                            startActivity(intent)

                        } else {
                            resultTextView.text = "Ошибка входа: ${result.message}"
                            Toast.makeText(this@MainActivity, "Неверные данные", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        resultTextView.text = "Ошибка сервера: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    resultTextView.text = "Ошибка сети: ${t.message}"
                    Toast.makeText(this@MainActivity, "Проверьте подключение", Toast.LENGTH_SHORT).show()
                }
            })
        }

        registerButton.setOnClickListener {
            try {
                val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}


data class UserData(
    val Name: String,
    val Password: String
)

data class ApiResponse(
    val status: String,  // Ожидаем "True" или "False"
    val message: String,
    val received_data: ReceivedData? = null
) {
    data class ReceivedData(
        val name: String,
        val password: String
    )
}

interface ApiServices {
    @POST("receive-data")
    fun sendData(@Body userData: UserData): Call<ApiResponse>
}