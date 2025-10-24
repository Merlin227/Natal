package com.example.natal


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import com.example.natal.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class RegistrationActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        val editTextLogin = findViewById<EditText>(R.id.regTextLog)
        val editTextPassword = findViewById<EditText>(R.id.regTextPas)
        val editTextName = findViewById<EditText>(R.id.regTextName)
        val editTextItem1 = findViewById<Spinner>(R.id.spinner)
        val editTextBirthTime = findViewById<EditText>(R.id.regTextTime)
        val editTextBirthDate = findViewById<EditText>(R.id.regTextDate)
        val buttonRegister = findViewById<Button>(R.id.button)
        val buttonLogin = findViewById<Button>(R.id.button2)

        setupSpinner(editTextItem1)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        buttonRegister.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()
            val name = editTextName.text.toString()
            val item1 = editTextItem1.selectedItem.toString()
            val birthTime = editTextBirthTime.text.toString()
            val birthDate = editTextBirthDate.text.toString()

            if (login.isEmpty() || password.isEmpty() || name.isEmpty() || item1.isEmpty() || birthTime.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = UserDate(
                Login = login,
                Password = password,
                Name = name,
                Item1 = item1,
                BirthTime = birthTime,
                BirthDate = birthDate
            )
            apiService.sendData(userData).enqueue(object : Callback<ApiResponses> {
                override fun onResponse(call: Call<ApiResponses>, response: Response<ApiResponses>) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!

                        if (result.status == "True") {
                            Toast.makeText(this@RegistrationActivity, "Успешная регистрация!", Toast.LENGTH_SHORT).show()

                            editTextLogin.text.clear()
                            editTextPassword.text.clear()
                            editTextName.text.clear()
                            editTextBirthTime.text.clear()
                            editTextBirthDate.text.clear()

                            val intent = Intent(this@RegistrationActivity, FirstActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this@RegistrationActivity, "Ошибка регистрации: ${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponses>, t: Throwable) {
                    Toast.makeText(this@RegistrationActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        buttonLogin.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupSpinner(spinner: Spinner) {
        // Создаем адаптер из массива в strings.xml
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        )

        // Устанавливаем layout для выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Применяем адаптер к Spinner
        spinner.adapter = adapter
    }
}

data class UserDate(
    val Login: String,
    val Password: String,
    val Name: String,
    val Item1: String,
    val BirthTime: String,
    val BirthDate: String

)

data class ApiResponses(
    val status: String,  // "True" или "False"
    val message: String,
    val received_data: ReceivedData? = null
) {
    data class ReceivedData(
        val login: String,
        val password: String,
        val name: String,
        val item1: String,
        val birthTime: String,
        val birthDate: String
    )
}

interface ApiService {
    @POST("registration")
    fun sendData(@Body userData: UserDate): Call<ApiResponses>
}