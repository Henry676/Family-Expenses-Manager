package com.example.administrador_gastos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorExtras constructor( var dato: MutableList<String>, var layout: Int, var itemListener: AdaptadorExtras.OnItemClickListener): RecyclerView.Adapter<AdaptadorExtras.ViewHolder?>(){
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var txtRegistro: TextView = itemView.findViewById<View>(R.id.textViewName)as TextView
        fun bind(name: String?, itemListener: AdaptadorExtras.OnItemClickListener) {
            txtRegistro.text = name
            txtRegistro.setTextColor(Color.WHITE)
            itemView.setBackgroundColor(Color.BLACK)
            itemView.setOnClickListener {
                itemListener.onItemClick(name, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorExtras.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val vh = AdaptadorExtras.ViewHolder(view)
        return vh
    }

    override fun getItemCount(): Int {
        return dato.size;
    }

    override fun onBindViewHolder(holder: AdaptadorExtras.ViewHolder, position: Int) {
        holder.bind(dato.get(position), itemListener);
    }
    interface OnItemClickListener {
        fun onItemClick(name: String?, position: Int)
    }
}