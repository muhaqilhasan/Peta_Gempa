package id.akaruuu.petagempaleaflet

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

class NetworkConfig {
    fun getInterceptor(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Instance untuk Gempa (XML/JSON Lama)
    fun getGempaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://data.bmkg.go.id/DataMKG/TEWS/")
            .client(getInterceptor())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instance untuk Cuaca (API Baru)
    fun getCuacaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.bmkg.go.id/publik/")
            .client(getInterceptor())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getGempaService(): GempaService {
        return getGempaRetrofit().create(GempaService::class.java)
    }

    fun getCuacaService(): CuacaService {
        return getCuacaRetrofit().create(CuacaService::class.java)
    }
}

interface GempaService {
    @GET("gempaterkini.json")
    fun getGempaTerkini(): Call<GempaResponse>
}

interface CuacaService {
    // API Baru: https://api.bmkg.go.id/publik/prakiraan-cuaca?adm4=31.71.03.1001
    @GET("prakiraan-cuaca")
    fun getCuacaByAdm4(@Query("adm4") adm4: String): Call<CuacaResponse>
}

object ApiClient {
    val gempaService: GempaService by lazy {
        NetworkConfig().getGempaService()
    }
    val cuacaService: CuacaService by lazy {
        NetworkConfig().getCuacaService()
    }
}

// Model Gempa (Tetap sama)
data class GempaResponse(val Infogempa: InfoGempa)
data class InfoGempa(val gempa: List<Gempa>)
data class Gempa(
    val Tanggal: String, val Jam: String, val DateTime: String,
    val Coordinates: String, val Lintang: String, val Bujur: String,
    val Magnitude: String, val Kedalaman: String, val Wilayah: String, val Potensi: String
)