package id.akaruuu.petagempaleaflet

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import id.akaruuu.petagempaleaflet.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gempaAdapter: GempaAdapter

    private var gempaList: List<Gempa> = listOf()

    private var activeView = 1
    private var systemBarTopInset = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastUserLat: Double = -6.175 // Default Jakarta
    private var lastUserLng: Double = 106.828 // Default Jakarta

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                getUserLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Window Insets (Status Bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            systemBarTopInset = insets.top
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            binding.bottomNavContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = 24 + insets.bottom }
            WindowInsetsCompat.CONSUMED
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupWebView()
        setupGempaList()
        setupBottomNav()
        setupLatestCardListener()

        // Fetch Data Awal
        fetchGempaData()

        binding.btnRefresh.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (activeView == 3) {
                fetchCuacaByLocation(lastUserLat, lastUserLng)
            } else {
                fetchGempaData()
                getUserLocation()
            }
        }

        checkAndRequestLocation()
    }

    private fun checkAndRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastUserLat = location.latitude
                    lastUserLng = location.longitude

                    binding.webViewMap.evaluateJavascript("updateUserLocation($lastUserLat, $lastUserLng)", null)

                    if (activeView == 3) fetchCuacaByLocation(lastUserLat, lastUserLng)
                }
            }
    }

    private fun fetchCuacaByLocation(lat: Double, lng: Double) {
        val nearest = LocationMapping.getNearestAdm4(this, lat, lng)

        ApiClient.cuacaService.getCuacaByAdm4(nearest.id).enqueue(object : Callback<CuacaResponse> {
            override fun onResponse(call: Call<CuacaResponse>, response: Response<CuacaResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    if (data.isNotEmpty()) {
                        val cuacaData = data[0]
                        val namaLokasi = "${cuacaData.lokasi.kecamatan}, ${cuacaData.lokasi.kotkab}"

                        if (cuacaData.cuaca.isNotEmpty()) {
                            val todayForecast = cuacaData.cuaca[0]
                            val currentDetail = findNearestForecast(todayForecast)

                            updateWeatherUI(namaLokasi, currentDetail)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Data cuaca tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal memuat cuaca: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CuacaResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("CUACA", "Error: ${t.message}")
            }
        })
    }

    private fun findNearestForecast(forecasts: List<CuacaDetail>): CuacaDetail? {
        if (forecasts.isEmpty()) return null

        val now = Date()
        var nearest: CuacaDetail? = null
        var minDiff = Long.MAX_VALUE

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (item in forecasts) {
            try {
                val itemDate = format.parse(item.datetime)
                if (itemDate != null) {
                    val diff = Math.abs(now.time - itemDate.time)
                    if (diff < minDiff) {
                        minDiff = diff
                        nearest = item
                    }
                }
            } catch (e: Exception) { continue }
        }

        return nearest ?: forecasts[0]
    }

    private fun updateWeatherUI(kota: String, detail: CuacaDetail?) {
        if (detail == null) return

        binding.tvCuacaKota.text = kota
        binding.tvCuacaSuhu.text = "${detail.t}°"
        binding.tvCuacaDesc.text = WeatherCodeMapper.getDesc(detail.weather)
        binding.tvCuacaAngin.text = "${detail.ws} km/j"
        binding.tvCuacaLembap.text = "${detail.hu}%"

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val idLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
            val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy • HH:mm", idLocale)
            val date = inputFormat.parse(detail.datetime)
            binding.tvCuacaTanggal.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            binding.tvCuacaTanggal.text = detail.datetime
        }

        val bgLayout = binding.bgCuaca.parent as androidx.cardview.widget.CardView
        if (detail.weather >= 60) {
            bgLayout.setCardBackgroundColor(Color.parseColor("#3B82F6"))
        } else if (detail.weather <= 2) {
            bgLayout.setCardBackgroundColor(Color.parseColor("#F59E0B"))
        } else {
            bgLayout.setCardBackgroundColor(Color.parseColor("#64748B"))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webViewMap.settings.javaScriptEnabled = true
        binding.webViewMap.settings.domStorageEnabled = true
        binding.webViewMap.webChromeClient = WebChromeClient()
        binding.webViewMap.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.181 Mobile Safari/537.36"

        binding.webViewMap.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (gempaList.isNotEmpty()) {
                    sendDataToMap(gempaList)
                }
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                }
            }
        }
        binding.webViewMap.addJavascriptInterface(WebAppInterface(), "Android")
        binding.webViewMap.loadUrl("file:///android_asset/map.html")
    }

    private fun setupGempaList() {
        gempaAdapter = GempaAdapter { gempa -> showDetailBottomSheet(gempa) }
        binding.rvGempa.layoutManager = LinearLayoutManager(this)
        binding.rvGempa.adapter = gempaAdapter
    }

    private fun setupBottomNav() {
        binding.btnNavMap.setOnClickListener { switchView(1) }
        binding.btnNavList.setOnClickListener { switchView(2) }
        binding.btnNavCuaca.setOnClickListener { switchView(3) }
    }

    private fun setupLatestCardListener() {
        binding.cvLatestGempa.setOnClickListener {
            if (gempaList.isNotEmpty()) showDetailBottomSheet(gempaList[0])
        }
    }

    private fun switchView(viewId: Int) {
        activeView = viewId
        val activeColor = ColorStateList.valueOf(Color.parseColor("#0891B2"))
        val inactiveColor = ColorStateList.valueOf(Color.parseColor("#94A3B8"))

        ImageViewCompat.setImageTintList(binding.iconMap, inactiveColor)
        binding.textMap.setTextColor(inactiveColor)
        ImageViewCompat.setImageTintList(binding.iconList, inactiveColor)
        binding.textList.setTextColor(inactiveColor)
        ImageViewCompat.setImageTintList(binding.iconCuaca, inactiveColor)
        binding.textCuaca.setTextColor(inactiveColor)

        // Hide Content Views
        binding.webViewMap.visibility = View.GONE
        binding.layoutListGempa.visibility = View.GONE
        binding.layoutCuaca.visibility = View.GONE
        binding.cvLatestGempa.visibility = View.GONE

        // FIXED: Selalu Tampilkan App Bar (Header Info Bencana)
        binding.appBarLayout.visibility = View.VISIBLE

        when (viewId) {
            1 -> { // PETA
                binding.webViewMap.visibility = View.VISIBLE
                if (gempaList.isNotEmpty()) binding.cvLatestGempa.visibility = View.VISIBLE
            }
            2 -> { // LIST GEMPA
                // Tampilkan Container List Gempa (yg berisi Header + RV)
                binding.layoutListGempa.visibility = View.VISIBLE
            }
            3 -> { // CUACA
                binding.layoutCuaca.visibility = View.VISIBLE
                binding.layoutCuaca.setPadding(0, 0, 0, 250) // Reset top padding since appbar is layout_behavior
                fetchCuacaByLocation(lastUserLat, lastUserLng)
            }
        }
    }

    private fun fetchGempaData() {
        ApiClient.gempaService.getGempaTerkini().enqueue(object : Callback<GempaResponse> {
            override fun onResponse(call: Call<GempaResponse>, response: Response<GempaResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        gempaList = body.Infogempa.gempa
                        gempaAdapter.setData(gempaList)
                        sendDataToMap(gempaList)
                        if (gempaList.isNotEmpty()) updateLatestCard(gempaList[0])
                    }
                }
            }
            override fun onFailure(call: Call<GempaResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("API_GEMPA", "Error: ${t.message}")
            }
        })
    }

    private fun updateLatestCard(gempa: Gempa) {
        if (activeView == 1) binding.cvLatestGempa.visibility = View.VISIBLE
        binding.tvLatestMag.text = gempa.Magnitude
        binding.tvLatestWilayah.text = gempa.Wilayah
        binding.tvLatestTime.text = "${gempa.Jam}, ${gempa.Tanggal}"
        binding.tvLatestDepth.text = "Kedalaman: ${gempa.Kedalaman}"

        val mag = gempa.Magnitude.toDoubleOrNull() ?: 0.0
        val color = when {
            mag >= 6.0 -> Color.parseColor("#EF4444")
            mag >= 5.0 -> Color.parseColor("#F97316")
            else -> Color.parseColor("#22C55E")
        }
        val bgShape = binding.bgLatestMag.background as GradientDrawable
        bgShape.setColor(color)
    }

    private fun sendDataToMap(list: List<Gempa>) {
        val gson = Gson()
        val jsonString = gson.toJson(list)
        binding.webViewMap.evaluateJavascript("updateMapFromAndroid('$jsonString')", null)
    }

    private fun showDetailBottomSheet(gempa: Gempa) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_detail, null)

        view.findViewById<MaterialTextView>(R.id.sheetWilayah).text = gempa.Wilayah
        view.findViewById<MaterialTextView>(R.id.sheetMag).text = "${gempa.Magnitude} SR"
        view.findViewById<MaterialTextView>(R.id.sheetTime).text = "${gempa.Tanggal}, ${gempa.Jam}"
        view.findViewById<MaterialTextView>(R.id.sheetDepth).text = "Kedalaman: ${gempa.Kedalaman}"
        view.findViewById<MaterialTextView>(R.id.sheetPotensi).text = gempa.Potensi
        view.findViewById<TextView>(R.id.sheetCoords).text = gempa.Coordinates

        val tvPotensi = view.findViewById<MaterialTextView>(R.id.sheetPotensi)
        if (gempa.Potensi.contains("TSUNAMI", ignoreCase = true)) {
            tvPotensi.setTextColor(Color.RED)
            tvPotensi.setBackgroundColor(Color.parseColor("#FEF2F2"))
        } else {
            tvPotensi.setTextColor(Color.parseColor("#15803D"))
            tvPotensi.setBackgroundColor(Color.parseColor("#F0FDF4"))
        }

        val btnSeeMap = view.findViewById<Button>(R.id.btnSeeOnMap)
        btnSeeMap.setOnClickListener {
            dialog.dismiss()
            if (activeView != 1) switchView(1)
            try {
                val coords = gempa.Coordinates.split(",")
                val lat = coords[0].trim()
                val lng = coords[1].trim()
                binding.webViewMap.evaluateJavascript("focusToLocation($lat, $lng)", null)
            } catch (e: Exception) {
                Toast.makeText(this, "Koordinat tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onMarkerClick(index: Int) {
            runOnUiThread {
                if (index < gempaList.size) {
                    showDetailBottomSheet(gempaList[index])
                }
            }
        }
    }
}