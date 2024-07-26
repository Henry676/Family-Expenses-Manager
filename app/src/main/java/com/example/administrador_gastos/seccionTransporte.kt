package com.example.administrador_gastos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class seccionTransporte : AppCompatActivity() {
    private val listaGastos = mutableListOf<String>()
    var agregar: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seccion_transporte)
        val boton1: Button = findViewById(R.id.addRecyclerTransporte)
        val boton2: Button = findViewById(R.id.showRecyclerTransp)
        val txttransporte: EditText = findViewById(R.id.txtTipo)
        val txtccantidad: EditText = findViewById(R.id.cantidadTransp)
        val txtdate: EditText = findViewById(R.id.txtFechaTransporte)
        val txtcosto: EditText = findViewById(R.id.txtTotalTransporte)

        txtdate.setOnClickListener{ showDatePickerDialog()}

        txttransporte.error = null
        txtccantidad.error = null
        txtdate.error = null
        txtcosto.error = null

        boton1.setOnClickListener {
            agregar++
            var campo1 = true
            var campo2 = true
            var campo3 = true
            var campo4 = true
            if (txttransporte.text.toString().isEmpty()){
                campo1 = false
                txttransporte.error = "Campo requerido"
            }
            if (txtccantidad.text.toString().isEmpty()){
                campo2 = false
                txtccantidad.error = "Campo requerido"
            }
            if (txtdate.text.toString().isEmpty()){
                campo3 = false
                txtdate.error = "Campo requerido"
            }
            if (txtcosto.text.toString().isEmpty()){
                campo4 = false
                txtcosto.error = "Campo requerido"
            }
            if (campo1 && campo2 && campo3 && campo4) {
                agregarGasto()
                Toast.makeText(this@seccionTransporte, "Gasto registrado", Toast.LENGTH_LONG).show()
            }
        }

        boton2.setOnClickListener {
            var campo1 = true
            var campo2 = true
            var campo3 = true
            var campo4 = true
            if (txttransporte.text.toString().isEmpty()){
                campo1 = false
                txttransporte.error = "Campo requerido"
            }
            if (txtccantidad.text.toString().isEmpty()){
                campo2 = false
                txtccantidad.error = "Campo requerido"
            }
            if (txtdate.text.toString().isEmpty()){
                campo3 = false
                txtdate.error = "Campo requerido"
            }
            if (txtcosto.text.toString().isEmpty()){
                campo4 = false
                txtcosto.error = "Campo requerido"
            }
            if (campo1 && campo2 && campo3 && campo4 && agregar > 0) {
                abrirRecyclerView()
                Toast.makeText(this@seccionTransporte, "Accediendo a los últimos gastos registrados", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun showDatePickerDialog() {
        val datePicker= DatePickerFragment {day,month,year->onDateSelected(day,month,year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    fun onDateSelected(day:Int, month: Int, year: Int){
        val txtdate: EditText = findViewById(R.id.txtFechaTransporte)
        txtdate.setText("$day/$month/$year")
    }

    private fun agregarGasto() {
        // Obtener referencias a los EditText
        val txtTransp: EditText = findViewById(R.id.txtTipo)
        val txtccantidad: EditText = findViewById(R.id.cantidadTransp)
        val txtdate: EditText = findViewById(R.id.txtFechaTransporte)
        val txtcosto: EditText = findViewById(R.id.txtTotalTransporte)

        // Crear el string del gasto y agregarlo a la lista
        val nombre = txtTransp.text.toString()
        val cantidad = txtccantidad.text.toString()
        val fecha = txtdate.text.toString()
        val total = txtcosto.text.toString()

        val gasto = "Nombre: $nombre, cantidad: $cantidad, Fecha: $fecha, Total: $total"
        listaGastos.add(gasto)
    }

    private fun abrirRecyclerView() {
        // Abrir la actividad del RecyclerView y pasar la lista de gastos
        val intent = Intent(this@seccionTransporte, RecyclerTransporte::class.java)
        intent.putStringArrayListExtra("listaGastos", ArrayList(listaGastos))
        startActivity(intent)
    }
}