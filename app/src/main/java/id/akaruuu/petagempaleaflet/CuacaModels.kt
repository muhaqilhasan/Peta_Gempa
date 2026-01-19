package id.akaruuu.petagempaleaflet

import com.google.gson.annotations.SerializedName

// --- Model untuk API Response (api.bmkg.go.id) ---

data class CuacaResponse(
    @SerializedName("data")
    val data: List<CuacaData>
)

data class CuacaData(
    @SerializedName("lokasi")
    val lokasi: Lokasi,
    @SerializedName("cuaca")
    val cuaca: List<List<CuacaDetail>>
)

data class Lokasi(
    @SerializedName("adm4")
    val adm4: String,
    @SerializedName("kotkab")
    val kotkab: String,
    @SerializedName("kecamatan")
    val kecamatan: String,
    @SerializedName("desa")
    val desa: String
)

data class CuacaDetail(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("t")
    val t: Int, // Suhu
    @SerializedName("weather")
    val weather: Int, // Kode Cuaca
    @SerializedName("ws")
    val ws: Double, // Kecepatan Angin
    @SerializedName("hu")
    val hu: Int // Kelembapan
)

// --- Helper Model (Opsional, ditambahkan kembali untuk mencegah error legacy) ---
data class CuacaUiModel(
    val kota: String,
    val suhu: String,
    val cuacaDesc: String,
    val waktu: String,
    val code: Int
)

object WeatherCodeMapper {
    fun getDesc(code: Int): String {
        return when (code) {
            0 -> "Cerah"
            1 -> "Cerah Berawan"
            2 -> "Cerah Berawan"
            3 -> "Berawan"
            4 -> "Berawan Tebal"
            5 -> "Udara Kabur"
            10 -> "Asap"
            45 -> "Kabut"
            60 -> "Hujan Ringan"
            61 -> "Hujan Sedang"
            63 -> "Hujan Lebat"
            80 -> "Hujan Petir"
            95 -> "Hujan Petir"
            97 -> "Hujan Petir"
            else -> "Tidak Diketahui"
        }
    }
}