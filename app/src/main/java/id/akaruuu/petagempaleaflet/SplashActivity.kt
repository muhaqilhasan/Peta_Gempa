package id.akaruuu.petagempaleaflet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Full Screen (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_splash)

        // Timer selama 2 detik (2000ms) sebelum pindah ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Menutup SplashActivity agar tidak bisa kembali dengan tombol back
        }, 2000)
    }
}