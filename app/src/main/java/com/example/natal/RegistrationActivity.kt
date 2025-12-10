package com.example.natal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
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

class RegistrationActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        val editTextLogin = findViewById<EditText>(R.id.regTextLog)
        val editTextPassword = findViewById<EditText>(R.id.regTextPas)
        val editTextItem1 = findViewById<Spinner>(R.id.spinner)
        val editTextBirthTime = findViewById<TextInputEditText>(R.id.timeEditText)
        val editTextBirthDate = findViewById<TextInputEditText>(R.id.regTextDate)
        val birthTimeLayout = findViewById<TextInputLayout>(R.id.layout_birth_time)
        val birthDateLayout = findViewById<TextInputLayout>(R.id.layout_birth_date)
        val buttonRegister = findViewById<Button>(R.id.button)
        val buttonLogin = findViewById<Button>(R.id.button2)

        setupSpinner(editTextItem1)
        setupDateInput(editTextBirthDate, birthDateLayout)
        setupTimeInput(editTextBirthTime, birthTimeLayout)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        buttonRegister.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()

            if (login.equals("admin", ignoreCase = true) && password == "admin") {
                Toast.makeText(this, "Недопустимые учетные данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item1 = editTextItem1.selectedItem.toString()
            val birthTime = editTextBirthTime.text.toString()
            val birthDate = editTextBirthDate.text.toString()

            if (login.isEmpty() || password.isEmpty() ||  item1.isEmpty() || birthTime.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = UserDate(
                Login = login,
                Password = password,
                Item1 = item1,
                BirthTime = birthTime,
                BirthDate = birthDate
            )
            apiService.sendData(userData).enqueue(object : Callback<ApiResponses> {
                override fun onResponse(call: Call<ApiResponses>, response: Response<ApiResponses>) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!

                        if (result.status == "True") {

                            val name = editTextLogin.text.toString()
                            val password = editTextPassword.text.toString()
                            Toast.makeText(this@RegistrationActivity, "Успешная регистрация!", Toast.LENGTH_SHORT).show()

                            editTextLogin.text.clear()
                            editTextPassword.text.clear()



                            val intent = Intent(this@RegistrationActivity, FirstActivity::class.java)

                            intent.putExtra("EXTRA_MESSAGE1", name)
                            intent.putExtra("EXTRA_MESSAGE2", password)

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
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            R.layout.spinner_item_dark
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        spinner.adapter = adapter
    }


    private fun setupDateInput(dateEditText: TextInputEditText, dateLayout: TextInputLayout) {
        addDateMask(dateEditText)

        dateLayout.setEndIconOnClickListener {
            showDatePickerDialog(dateEditText)
        }

        dateEditText.setOnClickListener {
            showDatePickerDialog(dateEditText)
        }
    }


    private fun setupTimeInput(timeEditText: TextInputEditText, timeLayout: TextInputLayout) {
        addTimeMask(timeEditText)

        timeLayout.setEndIconOnClickListener {
            showTimePickerDialog(timeEditText)
        }

        timeEditText.setOnClickListener {
            showTimePickerDialog(timeEditText)
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


    private fun addTimeMask(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (isUpdating) return

                val cleanString = s.toString().replace(":", "")


                if (cleanString.length > 6) {
                    isUpdating = true
                    editText.setText(cleanString.substring(0, 6))
                    editText.setSelection(6)
                    isUpdating = false
                    return
                }


                val formattedText = StringBuilder()
                for (i in cleanString.indices) {
                    formattedText.append(cleanString[i])
                    if ((i == 1 || i == 3) && i != cleanString.length - 1) {
                        formattedText.append(":")
                    }
                }


                if (cleanString.isNotEmpty() && cleanString.length < 6) {
                    when (cleanString.length) {
                        1 -> if (cleanString.toInt() > 2) formattedText.append(":00:00")
                        2 -> {
                            val hours = cleanString.toInt()
                            if (hours > 23) {
                                isUpdating = true
                                editText.setText("23:00:00")
                                editText.setSelection(8)
                                isUpdating = false
                                return
                            }
                            formattedText.append(":00:00")
                        }
                        3 -> formattedText.append(":00")
                        4 -> {
                            val minutes = cleanString.substring(2, 4).toInt()
                            if (minutes > 59) {
                                isUpdating = true
                                val fixed = cleanString.substring(0, 2) + "59"
                                editText.setText("${fixed.substring(0, 2)}:${fixed.substring(2)}:00")
                                editText.setSelection(8)
                                isUpdating = false
                                return
                            }
                            formattedText.append(":00")
                        }
                        5 -> formattedText.append("0")
                    }
                }

                isUpdating = true
                editText.setText(formattedText.toString())
                editText.setSelection(formattedText.length)
                isUpdating = false
            }
        })
    }


    private fun showDatePickerDialog(dateEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateEditText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    private fun showTimePickerDialog(timeEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                selectedTime.set(Calendar.SECOND, 0)

                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                timeEditText.setText(timeFormat.format(selectedTime.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
}


data class UserDate(
    val Login: String,
    val Password: String,
    val Item1: String,
    val BirthTime: String,
    val BirthDate: String
)

data class ApiResponses(
    val status: String,
    val message: String,
    val received_data: ReceivedData? = null
) {
    data class ReceivedData(
        val login: String,
        val password: String,
        val item1: String,
        val birthTime: String,
        val birthDate: String
    )
}

interface ApiService {
    @POST("registration")
    fun sendData(@Body userData: UserDate): Call<ApiResponses>
}