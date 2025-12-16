package id.akaruuu.petagempaleaflet

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import id.akaruuu.petagempaleaflet.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GempaAdapter
    private var gempaList: List<Gempa> = listOf()
    private var isMapViewActive = true

    // Menyimpan tinggi status bar untuk padding list
    private var systemBarTopInset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Handle Padding Status Bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            systemBarTopInset = insets.top

            // Margin atas untuk Toolbar
            binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }

            // Margin bawah untuk Nav Container (di atas gesture bar)
            binding.bottomNavContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = 24 + insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }

        setupWebView()
        setupRecyclerView()
        setupBottomNav()
        setupLatestCardListener()

        fetchData()

        binding.btnRefresh.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            fetchData()
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
            }
        }
        binding.webViewMap.addJavascriptInterface(WebAppInterface(), "Android")
        binding.webViewMap.loadUrl("file:///android_asset/map.html")
    }

    private fun setupRecyclerView() {
        adapter = GempaAdapter { gempa ->
            showDetailBottomSheet(gempa)
        }
        binding.rvGempa.layoutManager = LinearLayoutManager(this)
        binding.rvGempa.adapter = adapter
    }

    private fun setupBottomNav() {
        binding.btnNavMap.setOnClickListener {
            switchView(isMap = true)
        }
        binding.btnNavList.setOnClickListener {
            switchView(isMap = false)
        }
    }

    private fun setupLatestCardListener() {
        binding.cvLatestGempa.setOnClickListener {
            if (gempaList.isNotEmpty()) {
                showDetailBottomSheet(gempaList[0])
            }
        }
    }

    private fun switchView(isMap: Boolean) {
        isMapViewActive = isMap
        if (isMap) {
            // --- MODE PETA ---

            // 1. Tampilkan Header
            binding.appBarLayout.visibility = View.VISIBLE

            // 2. Tampilkan Peta & Kartu Floating
            binding.webViewMap.visibility = View.VISIBLE
            if (gempaList.isNotEmpty()) {
                binding.cvLatestGempa.visibility = View.VISIBLE
            }

            // 3. Sembunyikan List
            binding.rvGempa.visibility = View.GONE

            // 4. Update Warna Navigasi
            ImageViewCompat.setImageTintList(binding.iconMap, ColorStateList.valueOf(Color.parseColor("#0891B2")))
            binding.textMap.setTextColor(Color.parseColor("#0891B2"))
            ImageViewCompat.setImageTintList(binding.iconList, ColorStateList.valueOf(Color.parseColor("#94A3B8")))
            binding.textList.setTextColor(Color.parseColor("#94A3B8"))
        } else {
            // --- MODE LIST (RIWAYAT) ---

            // 1. Sembunyikan Header
            binding.appBarLayout.visibility = View.GONE

            // 2. Sembunyikan Peta & Kartu Floating
            binding.webViewMap.visibility = View.GONE
            binding.cvLatestGempa.visibility = View.GONE

            // 3. Tampilkan List
            binding.rvGempa.visibility = View.VISIBLE

            // Atur padding atas RecyclerView agar konten tidak tertabrak status bar
            // + 20dp extra space agar tidak terlalu mepet
            binding.rvGempa.setPadding(
                binding.rvGempa.paddingLeft,
                systemBarTopInset + 40,
                binding.rvGempa.paddingRight,
                binding.rvGempa.paddingBottom
            )

            // 4. Update Warna Navigasi
            ImageViewCompat.setImageTintList(binding.iconMap, ColorStateList.valueOf(Color.parseColor("#94A3B8")))
            binding.textMap.setTextColor(Color.parseColor("#94A3B8"))
            ImageViewCompat.setImageTintList(binding.iconList, ColorStateList.valueOf(Color.parseColor("#0891B2")))
            binding.textList.setTextColor(Color.parseColor("#0891B2"))
        }
    }

    private fun fetchData() {
        ApiClient.instance.getGempaTerkini().enqueue(object : Callback<GempaResponse> {
            override fun onResponse(call: Call<GempaResponse>, response: Response<GempaResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        gempaList = body.Infogempa.gempa
                        adapter.setData(gempaList)
                        sendDataToMap(gempaList)

                        if (gempaList.isNotEmpty()) {
                            updateLatestCard(gempaList[0])
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GempaResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLatestCard(gempa: Gempa) {
        if (isMapViewActive) {
            binding.cvLatestGempa.visibility = View.VISIBLE
        }
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
            if (!isMapViewActive) {
                switchView(isMap = true)
            }
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