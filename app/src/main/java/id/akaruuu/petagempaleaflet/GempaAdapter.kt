package id.akaruuu.petagempaleaflet

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.akaruuu.petagempaleaflet.databinding.ItemGempaBinding

class GempaAdapter(private val onClick: (Gempa) -> Unit) : RecyclerView.Adapter<GempaAdapter.ViewHolder>() {

    private val listGempa = ArrayList<Gempa>()

    fun setData(newList: List<Gempa>) {
        listGempa.clear()
        listGempa.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGempaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listGempa[position])
    }

    override fun getItemCount() = listGempa.size

    inner class ViewHolder(private val binding: ItemGempaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(gempa: Gempa) {
            binding.tvMagnitude.text = gempa.Magnitude
            binding.tvWilayah.text = gempa.Wilayah
            binding.tvJam.text = "${gempa.Tanggal} • ${gempa.Jam}"
            binding.tvKedalaman.text = gempa.Kedalaman

            // Atur warna berdasarkan magnitude
            val mag = gempa.Magnitude.toDoubleOrNull() ?: 0.0
            val color = when {
                mag >= 6.0 -> Color.parseColor("#EF4444") // Red
                mag >= 5.0 -> Color.parseColor("#F97316") // Orange
                else -> Color.parseColor("#22C55E") // Green
            }

            // Ubah warna background lingkaran secara programmatically
            val bgShape = binding.root.findViewWithTag<android.view.View>("magCircle")?.background as? GradientDrawable
                ?: (binding.tvMagnitude.parent.parent as android.view.View).background as GradientDrawable

            bgShape.setColor(color)

            binding.root.setOnClickListener { onClick(gempa) }
        }
    }
}