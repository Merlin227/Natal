// HoroscopeActivity.kt
package com.example.natal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.GET

class HoroscopeActivity : AppCompatActivity() {

    private lateinit var horoscopeContainer: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horoscope)

        horoscopeContainer = findViewById(R.id.horoscopeContainer)

        // Инициализация Retrofit (используем тот же baseUrl что в вашем примере)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(HoroscopeApiService::class.java)

        // Загрузка данных при открытии активности
        loadHoroscopes(apiService)
    }

    private fun loadHoroscopes(apiService: HoroscopeApiService) {
        apiService.getHoroscopes().enqueue(object : Callback<HoroscopeResponse> {
            override fun onResponse(call: Call<HoroscopeResponse>, response: Response<HoroscopeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val horoscopeResponse = response.body()!!

                    if (horoscopeResponse.status == "True") {
                        // Успешно получили данные - отображаем их
                        displayHoroscopes(horoscopeResponse.horoscopes)
                    } else {
                        // Ошибка от сервера
                        showError("Ошибка: ${horoscopeResponse.message}")
                    }
                } else {
                    showError("Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HoroscopeResponse>, t: Throwable) {
                showError("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun displayHoroscopes(horoscopes: List<HoroscopeData>) {
        horoscopeContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        horoscopes.forEach { horoscope ->
            // Создаем элемент для каждого гороскопа
            val horoscopeItem = inflater.inflate(R.layout.item_horoscope, horoscopeContainer, false)

            // Находим элементы в макете
            val imageView = horoscopeItem.findViewById<ImageView>(R.id.horoscopeImage)
            val titleTextView = horoscopeItem.findViewById<TextView>(R.id.horoscopeTitle)
            val contentTextView = horoscopeItem.findViewById<TextView>(R.id.horoscopeContent)

            // Устанавливаем данные
            titleTextView.text = horoscope.title
            contentTextView.text = horoscope.content

            // Устанавливаем изображение в зависимости от заголовка
            setImageForHoroscope(horoscope.title, imageView)

            horoscopeContainer.addView(horoscopeItem)
        }
    }

    private fun setImageForHoroscope(title: String, imageView: ImageView) {
        val imageResId = when (title) {
            "Весы" -> R.drawable.libra_icon
            "Овен" -> R.drawable.libra_icon
            "Телец" -> R.drawable.libra_icon
            "Близнецы" -> R.drawable.libra_icon
            "Рак" -> R.drawable.libra_icon
            "Лев" -> R.drawable.libra_icon
            "Дева" -> R.drawable.libra_icon
            "Скорпион" -> R.drawable.libra_icon
            "Стрелец" -> R.drawable.libra_icon
            "Козерог" -> R.drawable.libra_icon
            "Водолей" -> R.drawable.libra_icon
            "Рыбы" -> R.drawable.libra_icon
            else -> R.drawable.libra_icon
        }

        imageView.setImageResource(imageResId)
    }

    private fun showError(message: String) {
        val errorView = TextView(this).apply {
            text = message
            setPadding(50, 50, 50, 50)
        }
        horoscopeContainer.addView(errorView)
    }

    // HoroscopeResponse.kt
    data class HoroscopeResponse(
        val status: String,
        val message: String,
        val horoscopes: List<HoroscopeData>
    )

    data class HoroscopeData(
        val title: String,
        val content: String
    )
    interface HoroscopeApiService {
        @GET("get-horoscopes") // или другой endpoint с вашего сервера
        fun getHoroscopes(): Call<HoroscopeResponse>
    }

}