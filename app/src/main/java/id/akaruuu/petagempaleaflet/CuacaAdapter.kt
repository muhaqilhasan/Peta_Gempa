package id.akaruuu.petagempaleaflet

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class CuacaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Dummy implementation to satisfy compiler
        return object : RecyclerView.ViewHolder(View(parent.context)) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Do nothing
    }

    override fun getItemCount(): Int = 0
}