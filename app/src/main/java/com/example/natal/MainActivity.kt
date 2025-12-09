package com.example.natal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import android.util.Log
import android.content.SharedPreferences
import android.content.Context
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Запрос разрешения для уведомлений (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        // Получение FCM токена при запуске
        getFCMToken()

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val sendButtonLogin = findViewById<Button>(R.id.loginButton)
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

            // Проверка на администратора
            if (name == "admin" && password == "admin") {
                // Администраторский вход
                Toast.makeText(this, "Вход как администратор", Toast.LENGTH_SHORT).show()

                // Открываем другое окно для администратора
                val adminIntent = Intent(this@MainActivity, AdminActivity::class.java)
                adminIntent.putExtra("ADMIN_MODE", true)
                startActivity(adminIntent)

                editTextName.text.clear()
                editTextPassword.text.clear()
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
                            Toast.makeText(this@MainActivity, "Успешный вход!", Toast.LENGTH_SHORT).show()

                            editTextName.text.clear()
                            editTextPassword.text.clear()

                            // Сохраняем данные пользователя
                            sharedPreferences.edit().apply {
                                putString("user_name", name)
                                putString("user_password", password)
                                apply()
                            }

                            // Отправляем токен на сервер
                            sendTokenToServer(name)

                            // Отправляем уведомление о входе
                            sendLoginNotification(name)

                            val intent = Intent(this@MainActivity, FirstActivity::class.java)
                            intent.putExtra("EXTRA_MESSAGE1", name)
                            intent.putExtra("EXTRA_MESSAGE2", password)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this@MainActivity, "Неверные данные", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
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

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Device token: $token")

                // Сохраняем токен в SharedPreferences
                sharedPreferences.edit().putString("fcm_token", token).apply()
            } else {
                Log.e("FCM", "Failed to get token", task.exception)
            }
        }
    }

    private fun sendTokenToServer(userName: String) {
        val token = sharedPreferences.getString("fcm_token", null)

        if (token != null) {
            val tokenApiService = Retrofit.Builder()
                .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenApiService::class.java)

            val tokenData = TokenData(
                user_name = userName,
                device_token = token,
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )

            tokenApiService.saveToken(tokenData).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Log.d("TOKEN", "Token saved to server")
                    } else {
                        Log.e("TOKEN", "Failed to save token: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("TOKEN", "Failed to save token", t)
                }
            })
        }
    }

    private fun sendLoginNotification(userName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(NotificationApiService::class.java)

        val notificationData = mapOf(
            "user_name" to userName,
            "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        apiService.sendLoginNotification(notificationData).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("NOTIFICATION", "Login notification sent")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("NOTIFICATION", "Failed to send login notification", t)
            }
        })
    }
}

data class ApiResponse(
    val status: String,
    val message: String,
    val received_data: ReceivedData? = null
) {
    data class ReceivedData(
        val name: String,
        val password: String
    )
}



data class TokenData(
    val user_name: String,
    val device_token: String,
    val timestamp: String
)

interface ApiServices {
    @POST("receive-data")
    fun sendData(@Body userData: UserData): Call<ApiResponse>
}

interface TokenApiService {
    @POST("save-token")
    fun saveToken(@Body tokenData: TokenData): Call<ApiResponse>
}

interface NotificationApiService {
    @POST("send-login-notification")
    fun sendLoginNotification(@Body data: Map<String, String>): Call<ApiResponse>
}