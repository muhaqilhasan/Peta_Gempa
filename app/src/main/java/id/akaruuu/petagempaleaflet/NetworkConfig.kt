package id.akaruuu.petagempaleaflet

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- 1. Data Models ---

data class GempaResponse(
    val Infogempa: InfoGempaData
)

data class InfoGempaData(
    val gempa: List<Gempa>
)

data class Gempa(
    val Tanggal: String,
    val Jam: String,
    val DateTime: String,
    val Coordinates: String,
    val Lintang: String,
    val Bujur: String,
    val Magnitude: String,
    val Kedalaman: String,
    val Wilayah: String,
    val Potensi: String
)

// --- 2. Retrofit Interface ---

interface ApiService {
    // Mengambil 15 gempa terkini (lebih mudah diparsing karena berupa List)
    @GET("DataMKG/TEWS/gempaterkini.json")
    fun getGempaTerkini(): Call<GempaResponse>
}

// --- 3. Retrofit Client Instance ---

object ApiClient {
    private const val BASE_URL = "https://data.bmkg.go.id/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}