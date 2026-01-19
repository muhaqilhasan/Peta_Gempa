package id.akaruuu.petagempaleaflet

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GempaAdapter(private val onItemClick: (Gempa) -> Unit) : RecyclerView.Adapter<GempaAdapter.ViewHolder>() {

    private val data = ArrayList<Gempa>()

    fun setData(items: List<Gempa>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMagnitude: TextView = view.findViewById(R.id.tvMagnitude)
        val bgMag: View = view.findViewById(R.id.bgMag) // ID Baru untuk background lingkaran
        val tvWilayah: TextView = view.findViewById(R.id.tvWilayah)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal) // Menampung Tanggal + Jam
        val tvKedalaman: TextView = view.findViewById(R.id.tvKedalaman)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gempa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gempa = data[position]

        holder.tvMagnitude.text = gempa.Magnitude
        holder.tvWilayah.text = gempa.Wilayah

        // GABUNGKAN Tanggal dan Jam di satu TextView agar rapi
        // Contoh: "20 Jan 2024, 14:30 WIB"
        holder.tvTanggal.text = "${gempa.Tanggal}, ${gempa.Jam}"

        holder.tvKedalaman.text = "Kedalaman: ${gempa.Kedalaman}"

        // Logika Warna Magnitude (Hijau, Orange, Merah)
        val mag = gempa.Magnitude.toDoubleOrNull() ?: 0.0
        val colorCode = when {
            mag >= 6.0 -> "#EF4444" // Merah (Bahaya)
            mag >= 5.0 -> "#F97316" // Orange (Waspada)
            else -> "#10B981"       // Hijau (Aman)
        }

        // Set warna background lingkaran
        holder.bgMag.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorCode))

        holder.itemView.setOnClickListener {
            onItemClick(gempa)
        }
    }

    override fun getItemCount(): Int = data.size
}