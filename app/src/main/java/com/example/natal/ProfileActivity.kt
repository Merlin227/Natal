package com.example.natal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.ImageButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileName: TextInputEditText
    private lateinit var profileBirthDate: TextInputEditText
    private lateinit var profileBirthTime: TextInputEditText
    private lateinit var profileCity: AppCompatSpinner
    private lateinit var currentPassword: TextInputEditText
    private lateinit var newPassword: TextInputEditText
    private lateinit var confirmPassword: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var birthDateLayout: TextInputLayout
    private lateinit var birthTimeLayout: TextInputLayout

    private var userName: String = ""
    private var userPassword: String = ""
    private val cities = mutableListOf<City>()
    private var selectedCityId: Int = 0
    private var currentCityName: String = ""

    private lateinit var apiService: ProfileApiService

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Log.d(TAG, "=== ProfileActivity started ===")

        try {
            // Получаем данные из Intent
            userName = intent.getStringExtra("EXTRA_MESSAGE1") ?: ""
            userPassword = intent.getStringExtra("EXTRA_MESSAGE2") ?: ""

            Log.d(TAG, "User data: name='$userName', pass length=${userPassword.length}")

            if (userName.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Ошибка: данные пользователя не получены", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Инициализация Retrofit - ТАК ЖЕ КАК В MainActivity
            val retrofit = Retrofit.Builder()
                .baseUrl("https://consciously-replete-ox.cloudpub.ru/") // ТОТ ЖЕ URL ЧТО В MainActivity
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                )
                .build()

            apiService = retrofit.create(ProfileApiService::class.java)
            Log.d(TAG, "Retrofit initialized with cloud server")

            initViews()
            setupDateAndTimeInputs()
            loadUserData()
            loadCities()

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка загрузки профиля: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        profileName = findViewById(R.id.profileName)
        profileBirthDate = findViewById(R.id.profileBirthDate)
        profileBirthTime = findViewById(R.id.profileBirthTime)
        profileCity = findViewById(R.id.profileCity)
        currentPassword = findViewById(R.id.currentPassword)
        newPassword = findViewById(R.id.newPassword)
        confirmPassword = findViewById(R.id.confirmPassword)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        birthDateLayout = findViewById(R.id.layout_birth_date)
        birthTimeLayout = findViewById(R.id.layout_birth_time)

        // Устанавливаем текущий пароль (только для отображения)
        currentPassword.setText(userPassword)

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    private fun setupDateAndTimeInputs() {
        // Настройка маски для даты
        addDateMask(profileBirthDate)

        // Обработчик для иконки календаря
        birthDateLayout.setEndIconOnClickListener {
            showDatePickerDialog(profileBirthDate)
        }

        // Обработчик клика по полю
        profileBirthDate.setOnClickListener {
            showDatePickerDialog(profileBirthDate)
        }

        // Настройка маски для времени
        addTimeMask(profileBirthTime)

        // Обработчик для иконки времени
        birthTimeLayout.setEndIconOnClickListener {
            showTimePickerDialog(profileBirthTime)
        }

        // Обработчик клика по полю
        profileBirthTime.setOnClickListener {
            showTimePickerDialog(profileBirthTime)
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

        // Если в поле уже есть дата, устанавливаем ее
        try {
            val dateStr = dateEditText.text.toString()
            if (dateStr.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr)
                if (date != null) {
                    calendar.time = date
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        // Если в поле уже есть время, устанавливаем его
        try {
            val timeStr = timeEditText.text.toString()
            if (timeStr.isNotEmpty()) {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val time = sdf.parse(timeStr)
                if (time != null) {
                    calendar.time = time
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

    private fun loadUserData() {
        progressBar.visibility = ProgressBar.VISIBLE

        val userData = UserData(
            Name = userName,
            Password = userPassword
        )

        apiService.getUserProfile(userData).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                progressBar.visibility = ProgressBar.GONE

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status == "True") {
                        displayProfileData(result.profile)
                        Toast.makeText(this@ProfileActivity, "Данные загружены", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Ошибка: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Показываем имя хотя бы
                        profileName.setText(userName)
                    }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Сервер не отвечает (код: ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                    // Показываем имя хотя бы
                    profileName.setText(userName)
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(
                    this@ProfileActivity,
                    "Ошибка подключения: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Connection error: ${t.message}")
                // Показываем имя хотя бы
                profileName.setText(userName)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun displayProfileData(profile: ProfileResponse.ProfileData) {
        try {
            // Имя пользователя
            val login = profile.login ?: userName
            profileName.setText(login)

            // Дата рождения
            val dateBirth = profile.date_birth ?: ""
            if (dateBirth.isNotEmpty()) {
                profileBirthDate.setText(dateBirth)
            }

            // Время рождения
            val timeBirth = profile.time_birth ?: ""
            if (timeBirth.isNotEmpty()) {
                profileBirthTime.setText(timeBirth)
            }

            // Город
            val city = profile.city
            if (city != null) {
                selectedCityId = city.id ?: 0
                currentCityName = city.name ?: ""

                // Устанавливаем город в спиннер после загрузки списка городов
                if (cities.isNotEmpty()) {
                    updateCitySpinner()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка отображения данных: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error displaying profile: ${e.message}")
        }
    }

    private fun loadCities() {
        // Используем тот же подход, что и в RegistrationActivity
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // Используем тот же облачный сервер
                val request = okhttp3.Request.Builder()
                    .url("https://consciously-replete-ox.cloudpub.ru/cities")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.getString("status") == "True") {
                                cities.clear()
                                val citiesArray = jsonResponse.getJSONArray("cities")
                                for (i in 0 until citiesArray.length()) {
                                    val city = citiesArray.getJSONObject(i)
                                    cities.add(City(
                                        id = city.getInt("id"),
                                        name = city.getString("name")
                                    ))
                                }
                                setupCitySpinner()
                                // Обновляем спиннер после загрузки данных пользователя
                                if (currentCityName.isNotEmpty()) {
                                    updateCitySpinner()
                                }
                            }
                        } catch (e: Exception) {
                            setupDefaultCities()
                        }
                    } else {
                        setupDefaultCities()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setupDefaultCities()
                }
            }
        }
    }

    private fun setupDefaultCities() {
        cities.clear()
        cities.add(City(1, "Москва"))
        cities.add(City(2, "Санкт-Петербург"))
        cities.add(City(3, "Новосибирск"))
        cities.add(City(4, "Екатеринбург"))
        cities.add(City(5, "Казань"))

        setupCitySpinner()
        if (currentCityName.isNotEmpty()) {
            updateCitySpinner()
        }
    }

    private fun setupCitySpinner() {
        if (cities.isNotEmpty()) {
            val cityNames = cities.map { it.name }.toTypedArray()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            profileCity.adapter = adapter

            profileCity.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (position in cities.indices) {
                        selectedCityId = cities[position].id
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // Ничего не делаем
                }
            }
        }
    }

    private fun updateCitySpinner() {
        if (currentCityName.isNotEmpty() && cities.isNotEmpty()) {
            val cityIndex = cities.indexOfFirst { it.name == currentCityName }
            if (cityIndex >= 0) {
                profileCity.setSelection(cityIndex)
            } else {
                // Если город не найден в списке, добавляем его
                cities.add(City(selectedCityId, currentCityName))
                setupCitySpinner()
                profileCity.setSelection(cities.size - 1)
            }
        }
    }

    private fun validateAndSaveProfile() {
        val newPass = newPassword.text.toString()
        val confirmPass = confirmPassword.text.toString()

        if (newPass.isNotEmpty() && newPass != confirmPass) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show()
            return
        }

        if (newPass.isNotEmpty() && newPass.length < 6) {
            Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_LONG).show()
            return
        }

        saveProfile()
    }

    private fun saveProfile() {
        progressBar.visibility = ProgressBar.VISIBLE

        val profileUpdate = ProfileUpdate(
            login = userName,
            password = userPassword,
            new_password = if (newPassword.text.toString().isNotEmpty()) newPassword.text.toString() else null,
            city_name = profileCity.selectedItem?.toString(),
            date_birth = profileBirthDate.text.toString(),
            time_birth = profileBirthTime.text.toString()
        )

        apiService.updateUserProfile(profileUpdate).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                progressBar.visibility = ProgressBar.GONE

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status == "True") {
                        val newPass = newPassword.text.toString()
                        if (newPass.isNotEmpty()) {
                            userPassword = newPass
                            currentPassword.setText(newPass)
                            newPassword.setText("")
                            confirmPassword.setText("")
                        }

                        Toast.makeText(
                            this@ProfileActivity,
                            "Профиль успешно обновлен",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Перезагружаем данные
                        loadUserData()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Ошибка: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Ошибка сети при обновлении (код: ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(
                    this@ProfileActivity,
                    "Ошибка: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    data class City(
        val id: Int,
        val name: String
    )
}

// Модели данных для Retrofit
data class UserData(
    val Name: String,
    val Password: String
)

data class ProfileResponse(
    val status: String,
    val message: String,
    val profile: ProfileData
) {
    data class ProfileData(
        val id_user: Int? = null,
        val login: String? = null,
        val date_birth: String? = null,
        val time_birth: String? = null,
        val city: CityData? = null
    ) {
        data class CityData(
            val id: Int? = null,
            val name: String? = null
        )
    }
}

data class ProfileUpdate(
    val login: String,
    val password: String,
    val new_password: String? = null,
    val city_name: String? = null,
    val date_birth: String? = null,
    val time_birth: String? = null
)

data class BaseResponse(
    val status: String,
    val message: String
)

// Интерфейс API
interface ProfileApiService {
    @POST("user/profile")
    fun getUserProfile(@Body userData: UserData): Call<ProfileResponse>

    @POST("user/profile/update")
    fun updateUserProfile(@Body profileUpdate: ProfileUpdate): Call<BaseResponse>
}