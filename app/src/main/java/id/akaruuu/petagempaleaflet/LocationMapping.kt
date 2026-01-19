package id.akaruuu.petagempaleaflet

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.*

data class Wilayah(
    val id: String, // adm4 code (Contoh: 31.71.01.1002)
    val kota: String,
    val lat: Double,
    val lon: Double
)

object LocationMapping {
    private var cachedWilayah: List<Wilayah>? = null

    // Fallback data (Jika CSV gagal dibaca, pakai data Jakarta Pusat)
    private val fallbackWilayah = listOf(
        Wilayah("31.71.01.1002", "Jakarta Pusat (Gambir)", -6.176, 106.824)
    )

    /**
     * Fungsi Utama: Menerjemahkan GPS (Lat/Lon) menjadi Wilayah BMKG terdekat
     */
    fun getNearestAdm4(context: Context, userLat: Double, userLon: Double): Wilayah {
        // 1. Load data CSV ke memori jika belum ada
        if (cachedWilayah == null || cachedWilayah!!.isEmpty()) {
            cachedWilayah = loadFromAssets(context)
        }

        val dataToSearch = if (!cachedWilayah.isNullOrEmpty()) cachedWilayah!! else fallbackWilayah

        var nearest: Wilayah = dataToSearch[0]
        var minDistance = Double.MAX_VALUE

        // 2. Cari titik dengan jarak terpendek (Rumus Haversine)
        for (w in dataToSearch) {
            val dist = calculateDistance(userLat, userLon, w.lat, w.lon)
            if (dist < minDistance) {
                minDistance = dist
                nearest = w
            }
        }

        Log.d("LocationMapping", "GPS: $userLat, $userLon -> Matched: ${nearest.kota} (${nearest.id}) Jarak: ${"%.2f".format(minDistance)}km")
        return nearest
    }

    private fun loadFromAssets(context: Context): List<Wilayah> {
        val list = mutableListOf<Wilayah>()
        try {
            // Membaca file 'wilayah.csv' dari folder assets
            val inputStream = context.assets.open("wilayah.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Baca header (baris pertama) dan abaikan
            reader.readLine()

            var line = reader.readLine()
            while (line != null) {
                // Format CSV: id,kota,lat,lon
                val tokens = line.split(",")
                if (tokens.size >= 4) {
                    try {
                        val id = tokens[0].trim()
                        val kota = tokens[1].trim()
                        val lat = tokens[2].trim().toDouble()
                        val lon = tokens[3].trim().toDouble()
                        list.add(Wilayah(id, kota, lat, lon))
                    } catch (e: Exception) {
                        Log.e("LocationMapping", "Skip invalid line: $line")
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("LocationMapping", "Gagal membaca wilayah.csv", e)
        }
        return list
    }

    // Menghitung jarak antar dua koordinat (dalam KM)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius bumi dalam km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}