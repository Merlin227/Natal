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


        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(HoroscopeApiService::class.java)


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
            "Весы" -> R.drawable.libra
            "Овен" -> R.drawable.aries
            "Телец" -> R.drawable.taurus
            "Близнецы" -> R.drawable.gemini
            "Рак" -> R.drawable.cancer
            "Лев" -> R.drawable.leo
            "Дева" -> R.drawable.virgo
            "Скорпион" -> R.drawable.scorpio
            "Стрелец" -> R.drawable.sagit
            "Козерог" -> R.drawable.cap
            "Водолей" -> R.drawable.aqua
            "Рыбы" -> R.drawable.pisces
            else -> R.drawable.el
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
        @GET("get-horoscopes")
        fun getHoroscopes(): Call<HoroscopeResponse>
    }

}