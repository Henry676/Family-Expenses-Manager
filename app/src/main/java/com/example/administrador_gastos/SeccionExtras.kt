package com.example.administrador_gastos

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.sql.SQLException

class SeccionExtras : AppCompatActivity() {
    private lateinit var bd : DatabaseReference
    private lateinit var txtExtras : EditText
    private lateinit var txtCantidad : EditText
    private lateinit var txtdate : EditText
    private lateinit var txtcosto : EditText
    private lateinit var dbAdmin : DBController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seccion_extras)

        bd = FirebaseDB.databaseReference.child("gastos")
        dbAdmin = DBController(this, "gastosCasa.db", null, 1)


        window.statusBarColor = ContextCompat.getColor(this, R.color.purple)// Color de status bar
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.purple))// Color de navigation bar

        val boton1: Button = findViewById(R.id.addRecyclerExtra)
        val boton2: Button = findViewById(R.id.showRecyclerExtra)
        val boton3: Button = findViewById(R.id.updateRecyclerExtra)


        txtExtras = findViewById(R.id.txtTipoExtra)
        txtCantidad = findViewById(R.id.txtCantidadExtra)
        txtdate = findViewById(R.id.txtFechaExtra)
        txtcosto = findViewById(R.id.txtTotalExtra)

        txtdate.setOnClickListener{ showDatePickerDialog()}

        txtExtras.error = null
        txtCantidad.error = null
        txtdate.error = null
        txtcosto.error = null

        boton1.setOnClickListener {

            if (validarCampos()) {
                agregarGasto()
                resetFields()
            }
        }

        boton2.setOnClickListener {
            consultarGastos()
        }
        boton3.setOnClickListener {
            if (validarCampos()) {
                modificarGasto()
                resetFields()
            }
        }

    }
    private fun showDatePickerDialog() {
        val datePicker= DatePickerFragment {day,month,year->onDateSelected(day,month,year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    fun onDateSelected(day:Int, month: Int, year: Int){
        var actMonth = month + 1
        txtdate.setText("$day/$actMonth/$year")
    }


    private fun validarCampos() : Boolean{
        var campos = true

        if (txtExtras.text.toString().isEmpty()){
            campos = false
            txtExtras.error = "Campo requerido"
        }
        if (txtCantidad.text.toString().isEmpty()){
            campos = false
            txtCantidad.error = "Campo requerido"
        }
        if (txtdate.text.toString().isEmpty()){
            campos = false
            txtdate.error = "Campo requerido"
        }
        if (txtcosto.text.toString().isEmpty()){
            campos = false
            txtcosto.error = "Campo requerido"
        }
        return campos
    }
    private fun abrirRecyclerView(gastos: List<String>) {
        val intent = Intent(this@SeccionExtras, RecyclerExtras::class.java)
        intent.putStringArrayListExtra("listaGastos", ArrayList(gastos))
        startActivity(intent)
    }
    private fun agregarGasto() {
        val nombre = txtExtras.text.toString()
        val cantidad = txtCantidad.text.toString()
        val fecha = txtdate.text.toString()
        val total = txtcosto.text.toString()

        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val gastoId = bd.push().key
        if (gastoId != null) {
            agregarIdGastoLocal(gastoId,nombre,emailUsuarioLogueado)

            val nuevoGasto = Gasto(
                id = gastoId,
                email = emailUsuarioLogueado,
                nomGasto = nombre,
                cantidad = cantidad.toInt(),
                dia = fecha,
                total = total.toFloat(),
                categoria = "Extras"
            )

            bd.child(gastoId).setValue(nuevoGasto).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Gasto registrado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al registrar el gasto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun agregarIdGastoLocal(gastoId: String, nombre: String, email: String){
        val db = dbAdmin.writableDatabase
        val record = ContentValues()
        record.put("id", gastoId)
        record.put("name", nombre)
        record.put("email",email)
        if(db != null){
            // Almacenar valores en la tabla
            try{
                val x: Long = db.insert("gastosFamiliares",null,record)
                Toast.makeText(this,"Registro guardado de manera local",Toast.LENGTH_SHORT).show()
            } catch(e: SQLException){
                Log.e("Exception","Error: " + e.message.toString())
                Toast.makeText(this,"Registro guardado de manera local",Toast.LENGTH_SHORT).show()
            }
            db.close()
        }
    }

    private fun consultarGastos() {
        // Obtener el email del usuario logeado
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email

        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        bd.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gastos = mutableListOf<String>()
                val result = task.result
                if (result != null && result.hasChildren()) { // Verifica si existen hijos
                    for (snapshot in result.children) {
                        val gasto = snapshot.getValue(Gasto::class.java)

                        if (gasto != null && gasto.categoria == "Extras" && gasto.email == emailUsuarioLogueado) {
                            val detalleGasto = "${gasto.nomGasto}: ${gasto.cantidad} - ${gasto.dia} - ${gasto.total}"
                            gastos.add(detalleGasto)
                        }
                    }
                    // Llama a abrirRecyclerView con los datos obtenidos
                    if(gastos.isEmpty()){
                        Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                    }else{
                        abrirRecyclerView(gastos)
                    }                  } else {
                    Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al cargar los gastos", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun modificarGasto() {
        val nombre = txtExtras.text.toString()
        val cantidad = txtCantidad.text.toString()
        val fecha = txtdate.text.toString()
        val total = txtcosto.text.toString()

        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbAdmin.readableDatabase

        val row = db.rawQuery(
            "SELECT id FROM gastosFamiliares WHERE name = ? AND email = ?",
            arrayOf(nombre,emailUsuarioLogueado), // Pasar el valor como parámetro
        )
        var localId = ""
        if (row.moveToFirst()) {
            localId = row.getString(0)
        }

        // Consultar Firebase para verificar si el gasto existe
        bd.child(localId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && result.exists()) {
                    // Actualizaciones parciales
                    val actualizaciones = mapOf<String, Any>(
                        //"nomGasto" to nombre,
                        "cantidad" to cantidad.toInt(),
                        "dia" to fecha,
                        "total" to total.toFloat(),
                        //"categoria" to "Extras"
                    )

                    // Actualizar los campos especificados
                    bd.child(localId).updateChildren(actualizaciones).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Gasto modificado correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al modificar el gasto", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El gasto no existe en Firebase", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al consultar el gasto en Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetFields() {
        txtExtras.setText("")
        txtCantidad.setText("")
        txtdate.setText("")
        txtcosto.setText("")
        txtExtras.requestFocus()
    }
}