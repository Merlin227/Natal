package com.example.natal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*



class CompatibilityActivity : AppCompatActivity() {

    private lateinit var birthDateEditText: TextInputEditText
    private lateinit var birthDateLayout: TextInputLayout
    private lateinit var buttonCompatibility: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compatibility) // Убедитесь, что ваш XML файл называется activity_first.xml

        // Инициализация элементов
        birthDateEditText = findViewById(R.id.regTextDate)
        birthDateLayout = findViewById(R.id.layout_birth_date)
        buttonCompatibility = findViewById(R.id.button_compatibility)

        // Получаем переданные данные из RegistrationActivity
        val username = intent.getStringExtra("EXTRA_MESSAGE1") ?: ""
        val password = intent.getStringExtra("EXTRA_MESSAGE2") ?: ""

        // Настройка ввода даты
        setupDateInput()

        // Обработчик кнопки "Продолжить"
        buttonCompatibility.setOnClickListener {
            val birthDate = birthDateEditText.text.toString()

            if (birthDate.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверка формата даты (ГГГГ-ММ-ДД)
            if (!isValidDateFormat(birthDate)) {
                Toast.makeText(this, "Неверный формат даты. Используйте ГГГГ-ММ-ДД", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Отправка данных на сервер
            sendCompatibilityData(username, password, birthDate)
        }
    }

    private fun setupDateInput() {
        // Добавляем маску ввода даты
        addDateMask(birthDateEditText)

        // Обработчик клика по иконке календаря
        birthDateLayout.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        // Обработчик клика по самому полю ввода
        birthDateEditText.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun addDateMask(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (isUpdating) return

                val cleanString = s.toString().replace("-", "")

                // Ограничиваем длину (8 цифр = ГГГГММДД)
                if (cleanString.length > 8) {
                    isUpdating = true
                    editText.setText(cleanString.substring(0, 8))
                    editText.setSelection(8)
                    isUpdating = false
                    return
                }

                // Форматируем строку с дефисами
                val formattedText = StringBuilder()
                for (i in cleanString.indices) {
                    formattedText.append(cleanString[i])
                    if ((i == 3 || i == 5) && i != cleanString.length - 1) {
                        formattedText.append("-")
                    }
                }

                isUpdating = true
                editText.setText(formattedText.toString())
                editText.setSelection(formattedText.length)
                isUpdating = false
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                birthDateEditText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun sendCompatibilityData(username: String, password: String, partnerBirthDate: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CompatibilityApiService::class.java)

        val compatibilityData = CompatibilityData(
            login = username,
            password = password,
            partner_birth_date = partnerBirthDate
        )

        apiService.sendCompatibilityData(compatibilityData).enqueue(object : Callback<CompatibilityResponse> {
            override fun onResponse(call: Call<CompatibilityResponse>, response: Response<CompatibilityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status == "True") {
                        Toast.makeText(this@CompatibilityActivity, "Данные успешно отправлены!", Toast.LENGTH_SHORT).show()

                        // Здесь можно перейти к следующему экрану с результатами совместимости
                        // val intent = Intent(this@FirstActivity, ResultActivity::class.java)
                        // intent.putExtra("COMPATIBILITY_RESULT", result.message)
                        // startActivity(intent)

                    } else {
                        Toast.makeText(this@CompatibilityActivity, "Ошибка: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CompatibilityActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CompatibilityResponse>, t: Throwable) {
                Toast.makeText(this@CompatibilityActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// Data класс для отправки данных совместимости
data class CompatibilityData(
    val login: String,
    val password: String,
    val partner_birth_date: String
)

// Data класс для ответа от сервера
data class CompatibilityResponse(
    val status: String,  // "True" или "False"
    val message: String,
    val compatibility_result: CompatibilityResult? = null
) {
    data class CompatibilityResult(
        val score: Int?,
        val description: String?
    )
}

// Интерфейс для API совместимости
interface CompatibilityApiService {
    @POST("compatibility") // Замените на ваш endpoint для совместимости
    fun sendCompatibilityData(@Body compatibilityData: CompatibilityData): Call<CompatibilityResponse>
}