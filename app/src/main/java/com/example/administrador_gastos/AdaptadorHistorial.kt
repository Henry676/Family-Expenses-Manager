package com.example.administrador_gastos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorHistorial(
    var items: MutableList<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorHistorial.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtViewName: TextView = itemView.findViewById(R.id.textViewName)

        fun bind(name: String, position: Int) {
            txtViewName.text = name
            itemView.setBackgroundColor(Color.parseColor("#77B6FD"));
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newData: MutableList<String>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
