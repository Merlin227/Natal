// HoroscopeActivity.kt
package com.example.natal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class HoroscopeActivity : AppCompatActivity() {

    private lateinit var horoscopeContainer: LinearLayout
    private var currentHoroscopes: List<HoroscopeData> = emptyList()
    private lateinit var buttonExport: ImageButton
    private lateinit var buttonRefresh: ImageButton
    private lateinit var cacheManager: HoroscopeCacheManager
    private lateinit var progressBar: ProgressBar
    private lateinit var lastUpdateTextView: TextView

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horoscope)

        cacheManager = HoroscopeCacheManager(this)

        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonExport = findViewById<ImageButton>(R.id.buttonExport)
        buttonRefresh = findViewById<ImageButton>(R.id.buttonRefresh)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        lastUpdateTextView = findViewById<TextView>(R.id.lastUpdateTextView)

        buttonBack.setOnClickListener { finish() }
        buttonExport.setOnClickListener { exportHoroscopes() }
        buttonRefresh.setOnClickListener { refreshHoroscopes() }

        horoscopeContainer = findViewById(R.id.horoscopeContainer)

        if (cacheManager.isCacheValid()) {
            loadFromCache()
        } else {
            loadFromServer()
        }

        updateLastUpdateInfo()
    }

    private fun refreshHoroscopes() {
        buttonRefresh.rotation = 0f
        buttonRefresh.animate().rotation(360f).setDuration(500).start()
        cacheManager.clearCache()
        loadFromServer()
        Toast.makeText(this, "Обновление гороскопов...", Toast.LENGTH_SHORT).show()
    }

    private fun loadFromCache() {
        progressBar.visibility = View.VISIBLE
        val cachedHoroscopes = cacheManager.getCachedHoroscopes()
        if (cachedHoroscopes != null && cachedHoroscopes.isNotEmpty()) {
            currentHoroscopes = cachedHoroscopes
            displayHoroscopes(currentHoroscopes)
            Toast.makeText(this, "Загружено из кэша", Toast.LENGTH_SHORT).show()
        } else {
            loadFromServer()
        }
        progressBar.visibility = View.GONE
    }

    private fun loadFromServer() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(HoroscopeApiService::class.java)

        apiService.getHoroscopes().enqueue(object : Callback<HoroscopeResponse> {
            override fun onResponse(call: Call<HoroscopeResponse>, response: Response<HoroscopeResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val horoscopeResponse = response.body()!!

                    if (horoscopeResponse.status == "True") {
                        currentHoroscopes = horoscopeResponse.horoscopes
                        cacheManager.saveHoroscopes(currentHoroscopes)
                        updateLastUpdateInfo()
                        displayHoroscopes(currentHoroscopes)
                        Toast.makeText(this@HoroscopeActivity, "Гороскопы обновлены", Toast.LENGTH_SHORT).show()
                    } else {
                        loadFallbackFromCache("Ошибка: ${horoscopeResponse.message}")
                    }
                } else {
                    loadFallbackFromCache("Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HoroscopeResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                loadFallbackFromCache("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun loadFallbackFromCache(errorMessage: String) {
        val cachedHoroscopes = cacheManager.getCachedHoroscopes()
        if (cachedHoroscopes != null && cachedHoroscopes.isNotEmpty()) {
            currentHoroscopes = cachedHoroscopes
            displayHoroscopes(currentHoroscopes)
            Toast.makeText(this, "Используются кэшированные данные. $errorMessage", Toast.LENGTH_LONG).show()
        } else {
            showError(errorMessage)
        }
    }

    private fun displayHoroscopes(horoscopes: List<HoroscopeData>) {
        horoscopeContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        horoscopes.forEach { horoscope ->
            val horoscopeItem = inflater.inflate(R.layout.item_horoscope, horoscopeContainer, false)

            val imageView = horoscopeItem.findViewById<ImageView>(R.id.horoscopeImage)
            val titleTextView = horoscopeItem.findViewById<TextView>(R.id.horoscopeTitle)
            val contentTextView = horoscopeItem.findViewById<TextView>(R.id.horoscopeContent)

            titleTextView.text = horoscope.title
            contentTextView.text = horoscope.content

            setImageForHoroscope(horoscope.title, imageView)
            horoscopeContainer.addView(horoscopeItem)
        }

        buttonExport.isEnabled = horoscopes.isNotEmpty()
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
        horoscopeContainer.removeAllViews()

        val errorView = TextView(this).apply {
            text = message
            setPadding(50, 50, 50, 50)
            textSize = 16f
            gravity = android.view.Gravity.CENTER
        }

        val retryButton = TextView(this).apply {
            text = "Повторить попытку"
            setPadding(50, 20, 50, 20)
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
            setOnClickListener { refreshHoroscopes() }
        }

        horoscopeContainer.addView(errorView)
        horoscopeContainer.addView(retryButton)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLastUpdateInfo() {
        val lastUpdate = cacheManager.getLastUpdateDate()
        if (lastUpdate != null) {
            lastUpdateTextView.text = "Обновлено: $lastUpdate"
            lastUpdateTextView.visibility = View.VISIBLE
        } else {
            lastUpdateTextView.visibility = View.GONE
        }
    }

    private fun exportHoroscopes() {
        if (currentHoroscopes.isEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        val exportText = buildExportText()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Гороскопы на ${getCurrentDate()}")
            putExtra(Intent.EXTRA_TEXT, exportText)
        }

        startActivity(Intent.createChooser(shareIntent, "Экспортировать гороскопы"))
    }

    private fun buildExportText(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("✨ Гороскопы на ${getCurrentDate()} ✨\n")
        val lastUpdate = cacheManager.getLastUpdateDate()
        if (lastUpdate != null) {
            stringBuilder.append("(Обновлено: $lastUpdate)\n")
        }
        stringBuilder.append("\n")

        currentHoroscopes.forEachIndexed { index, horoscope ->
            stringBuilder.append("${index + 1}. ${horoscope.title}\n")
            stringBuilder.append("${horoscope.content}\n")
            stringBuilder.append("─".repeat(30))
            stringBuilder.append("\n\n")
        }

        return stringBuilder.toString()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}