package com.babilawi.buscaminas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

data class Personaje(val nombre: String, val imagenResId: Int)

class PersonajeAdapter(context: Context, resource: Int, private val personajes: List<Personaje>) :
    ArrayAdapter<Personaje>(context, resource, personajes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_personaje_spinner, parent, false)

        val personaje = getItem(position)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val imagenImageView: ImageView = itemView.findViewById(R.id.imagenImageView)

        nombreTextView.text = personaje?.nombre
        imagenImageView.setImageResource(personaje?.imagenResId ?: 0)

        return itemView
    }
}