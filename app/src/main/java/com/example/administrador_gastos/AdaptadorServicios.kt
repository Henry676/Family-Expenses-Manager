package com.example.administrador_gastos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorServicios constructor( var dato: MutableList<String>, var layout: Int, var itemListener: AdaptadorServicios.OnItemClickListener):
    RecyclerView.Adapter<AdaptadorServicios.ViewHolder?>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var txtRegistroServicio: TextView = itemView.findViewById<View>(R.id.textViewName)as TextView
        fun bind(name: String?, itemListener: AdaptadorServicios.OnItemClickListener) {
            txtRegistroServicio.text = name
            txtRegistroServicio.setTextColor(Color.WHITE)
            itemView.setBackgroundColor(Color.BLACK)
            itemView.setOnClickListener {
                itemListener.onItemClick(name, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorServicios.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val vh = AdaptadorServicios.ViewHolder(view)
        return vh
    }

    override fun getItemCount(): Int {
        return dato.size;
    }

    override fun onBindViewHolder(holder: AdaptadorServicios.ViewHolder, position: Int) {
        holder.bind(dato.get(position), itemListener);
    }
    interface OnItemClickListener {
        fun onItemClick(name: String?, position: Int)
    }
}