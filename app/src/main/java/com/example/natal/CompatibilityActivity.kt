package com.example.natal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
    private lateinit var buttonBack: ImageButton

    private lateinit var resultCardView: CardView
    private lateinit var percentageTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var closeResultButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compatibility)

        // Находим кнопку назад
        buttonBack = findViewById(R.id.buttonBack)

        // Обработка нажатия на кнопку назад
        buttonBack.setOnClickListener {
            finish() // Закрываем текущую Activity и возвращаемся назад
        }

        birthDateEditText = findViewById(R.id.regTextDate)
        birthDateLayout = findViewById(R.id.layout_birth_date)
        buttonCompatibility = findViewById(R.id.button_compatibility)

        resultCardView = findViewById(R.id.resultCardView)
        percentageTextView = findViewById(R.id.percentageTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        closeResultButton = findViewById(R.id.closeResultButton)

        val username = intent.getStringExtra("EXTRA_MESSAGE1") ?: ""
        val password = intent.getStringExtra("EXTRA_MESSAGE2") ?: ""

        setupDateInput()

        resultCardView.visibility = View.GONE

        buttonCompatibility.setOnClickListener {
            val birthDate = birthDateEditText.text.toString()

            if (birthDate.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(birthDate)) {
                Toast.makeText(this, "Неверный формат даты. Используйте ГГГГ-ММ-ДД", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendCompatibilityData(username, password, birthDate)
        }

        closeResultButton.setOnClickListener {
            resultCardView.visibility = View.GONE
            birthDateEditText.text?.clear()
        }
    }

    private fun setupDateInput() {
        addDateMask(birthDateEditText)

        birthDateLayout.setEndIconOnClickListener {
            showDatePickerDialog()
        }

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

                if (cleanString.length > 8) {
                    isUpdating = true
                    editText.setText(cleanString.substring(0, 8))
                    editText.setSelection(8)
                    isUpdating = false
                    return
                }

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
        buttonCompatibility.text = "Загрузка..."
        buttonCompatibility.isEnabled = false

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
                buttonCompatibility.text = "Продолжить"
                buttonCompatibility.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status == "True") {
                        val compatibilityResult = result.compatibility_result

                        if (compatibilityResult != null) {
                            showCompatibilityResult(
                                compatibilityResult.percentage ?: 0,
                                compatibilityResult.description ?: "Описание отсутствует"
                            )
                            Toast.makeText(this@CompatibilityActivity, "Расчет выполнен успешно!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CompatibilityActivity, "Результат не получен", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@CompatibilityActivity, "Ошибка: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CompatibilityActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CompatibilityResponse>, t: Throwable) {
                buttonCompatibility.text = "Продолжить"
                buttonCompatibility.isEnabled = true

                Toast.makeText(this@CompatibilityActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCompatibilityResult(percentage: Int, description: String) {
        percentageTextView.text = "$percentage%"
        descriptionTextView.text = description

        val color = when {
            percentage >= 80 -> "#4CAF50"
            percentage >= 60 -> "#FFC107"
            percentage >= 40 -> "#FF9800"
            else -> "#F44336"
        }

        percentageTextView.setTextColor(android.graphics.Color.parseColor(color))

        resultCardView.visibility = View.VISIBLE
        resultCardView.bringToFront()
    }
}

data class CompatibilityData(
    val login: String,
    val password: String,
    val partner_birth_date: String
)

data class CompatibilityResponse(
    val status: String,
    val message: String,
    val compatibility_result: CompatibilityResult? = null
)

data class CompatibilityResult(
    val percentage: Int?,
    val description: String?
)

interface CompatibilityApiService {
    @POST("compatibility")
    fun sendCompatibilityData(@Body compatibilityData: CompatibilityData): Call<CompatibilityResponse>
}