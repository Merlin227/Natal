// ApiService.kt
package com.example.natal

import retrofit2.Call
import retrofit2.http.GET

interface HoroscopeApiService {
    @GET("get-horoscopes")
    fun getHoroscopes(): Call<HoroscopeResponse>
}