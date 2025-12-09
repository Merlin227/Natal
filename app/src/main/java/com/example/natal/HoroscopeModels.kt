//// HoroscopeModels.kt
//package com.example.natal
//
//data class HoroscopeResponse(
//    val status: String,
//    val message: String,
//    val horoscopes: List<HoroscopeData>
//)
//
//data class HoroscopeData(
//    val title: String,
//    val content: String
//)
// Models.kt
package com.example.natal

data class HoroscopeResponse(
    val status: String,
    val message: String,
    val horoscopes: List<HoroscopeData>
)

data class HoroscopeData(
    val title: String,
    val content: String
)