package com.example.natal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class NatalActivity : AppCompatActivity() {

    private lateinit var planetsContainer: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_natal)



        planetsContainer = findViewById(R.id.natalContainer)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(PlanetsApiService::class.java)

        val name = intent.getStringExtra("EXTRA_MESSAGE1") ?: "user"
        val password = intent.getStringExtra("EXTRA_MESSAGE2") ?: "pass"


        loadPlanets(apiService, name, password)
    }

    private fun loadPlanets(apiService: PlanetsApiService, name: String, password: String) {
        val request = PlanetsRequest(name, password)

        apiService.getPlanets(request).enqueue(object : Callback<PlanetsResponse> {
            override fun onResponse(call: Call<PlanetsResponse>, response: Response<PlanetsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val planetsResponse = response.body()!!

                    if (planetsResponse.status == "True") {

                        displayPlanets(planetsResponse.planets)
                    } else {
                        showError("Ошибка: ${planetsResponse.message}")
                    }
                } else {
                    showError("Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlanetsResponse>, t: Throwable) {
                showError("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun displayPlanets(planets: List<PlanetData>) {
        planetsContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        planets.forEach { planet ->

            val planetItem = inflater.inflate(R.layout.item_planet, planetsContainer, false)


            val planetNameTextView = planetItem.findViewById<TextView>(R.id.planetName)
            val zodiacSignTextView = planetItem.findViewById<TextView>(R.id.zodiacSign)
            val housePositionTextView = planetItem.findViewById<TextView>(R.id.housePosition)
            val descriptionTextView = planetItem.findViewById<TextView>(R.id.planetDescription)


            planetNameTextView.text = planet.planetName
            zodiacSignTextView.text = planet.zodiacSign ?: "Не указано"
            housePositionTextView.text = planet.housePosition ?: "Не указано"
            descriptionTextView.text = planet.description

            planetsContainer.addView(planetItem)
        }
    }

    private fun showError(message: String) {
        val errorView = TextView(this).apply {
            text = message
            setTextColor(0xFFFF0000.toInt())
            setPadding(50, 50, 50, 50)
        }
        planetsContainer.addView(errorView)
    }


    data class PlanetsRequest(
        val name: String,
        val password: String
    )

    data class PlanetsResponse(
        val status: String,
        val message: String,
        val planets: List<PlanetData>
    )

    data class PlanetData(
        val planetName: String,
        val zodiacSign: String?,
        val housePosition: String?,
        val description: String
    )

    interface PlanetsApiService {
        @GET("get-planets")
        fun getPlanets(@Body request: PlanetsRequest): Call<PlanetsResponse>
    }
}

