# 🌏 Peta Gempa & Cuaca Indonesia

**Peta Gempa** adalah aplikasi Android berbasis Kotlin yang dirancang untuk memantau informasi gempa bumi terkini dan prakiraan cuaca di wilayah Indonesia secara *real-time*. Aplikasi ini memvisualisasikan lokasi gempa menggunakan peta interaktif.

## 📱 Fitur Utama

* **Info Gempa Terkini**: Menampilkan daftar gempa bumi terbaru dengan detail magnitudo, kedalaman, dan lokasi.
* **Peta Interaktif**: Visualisasi titik lokasi gempa menggunakan peta (Leaflet JS via WebView).
* **Informasi Cuaca**: Memantau prakiraan cuaca di berbagai wilayah.
* **Detail Wilayah**: Mendukung data wilayah Indonesia (menggunakan `wilayah.csv`).
* **Antarmuka Modern**: Menggunakan desain Material dengan dukungan navigasi yang mudah.

## 🛠️ Tech Stack

* **Bahasa**: [Kotlin](https://kotlinlang.org/)
* **Platform**: Android (Min SDK 24+)
* **Peta**: Leaflet.js (dimuat melalui WebView di `map.html`)
* **Arsitektur**: MVVM (Model-View-ViewModel) - *Disarankan*
* **Networking**: Retrofit / OkHttp (Untuk mengambil data gempa/cuaca)
* **Data Format**: JSON & CSV

---

## 📸 Tampilan Aplikasi (Screenshots)

Berikut adalah tampilan antarmuka dari aplikasi Peta Gempa:

| Splash Screen | Halaman Utama (Gempa) | Peta Lokasi (Leaflet) |
|:---:|:---:|:---:|
| <img src="assets/screenshots/splash.png" width="200" /> | <img src="assets/screenshots/home_gempa.png" width="200" /> | <img src="assets/screenshots/map_detail.png" width="200" /> |

| Info Cuaca | Detail Bottom Sheet |
|:---:|:---:|
| <img src="assets/screenshots/cuaca.png" width="200" /> | <img src="assets/screenshots/bottom_sheet.png" width="200" /> |

---

## 🎥 Demo Aplikasi

Simak bagaimana aplikasi ini bekerja secara langsung:

https://github.com/user-attachments/assets/video-demo.mp4

*(Catatan: Jika video tidak muncul, silakan unduh file di folder assets)*

---

## 🚀 Cara Menjalankan Project

1.  **Clone Repository**
    ```bash
    git clone [https://github.com/muhaqilhasan/peta_gempa.git](https://github.com/muhaqilhasan/peta_gempa.git)
    ```
2.  **Buka di Android Studio**
    * Buka Android Studio -> File -> Open -> Pilih folder `Peta_Gempa`.
3.  **Sync Gradle**
    * Tunggu hingga proses sinkronisasi dan indexing selesai.
4.  **Run App**
    * Sambungkan perangkat Android atau gunakan Emulator.
    * Klik tombol **Run (▶)**.

## 📂 Struktur Project

* `app/src/main/java/id/akaruuu/petagempaleaflet`: Kode utama Kotlin (Activity, Adapter, Models).
* `app/src/main/assets`: Berisi `map.html` (untuk peta) dan `wilayah.csv`.
* `app/src/main/res/layout`: File XML untuk tampilan antarmuka.

## 📄 Kredit

Dikembangkan oleh **Muhaqil Hasan**.

---
