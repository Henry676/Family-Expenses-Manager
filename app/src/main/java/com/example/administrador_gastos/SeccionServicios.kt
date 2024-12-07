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

class SeccionServicios : AppCompatActivity() {
    private lateinit var bd : DatabaseReference
    private lateinit var txtServicio : EditText
    private lateinit var txtdate : EditText
    private lateinit var txtcosto : EditText
    private lateinit var dbAdmin : DBController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seccion_servicios)

        bd = FirebaseDB.databaseReference.child("gastos")
        dbAdmin = DBController(this, "gastosCasa.db", null, 1)


        window.statusBarColor = ContextCompat.getColor(this, R.color.black)// Color de status bar
        supportActionBar?.setBackgroundDrawable(// Color de appBar
            ContextCompat.getDrawable(this, R.color.black)
        )
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black))// Color de navigation bar
        val boton1: Button = findViewById(R.id.addRecyclerServicio)
        val boton2: Button = findViewById(R.id.showRecyclerServicio)
        val boton3: Button = findViewById(R.id.updateRecyclerServicio)

        txtServicio = findViewById(R.id.txtTipoServicio)
        txtdate = findViewById(R.id.txtFechaServicio)
        txtcosto = findViewById(R.id.txtTotalServicio)

        txtdate.setOnClickListener{ showDatePickerDialog()}

        txtServicio.error = null
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
    private fun validarCampos() : Boolean{
        var campos = true
        if (txtServicio.text.toString().isEmpty()){
            campos = false
            txtServicio.error = "Campo requerido"
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

    private fun showDatePickerDialog() {
        val datePicker= DatePickerFragment {day,month,year->onDateSelected(day,month,year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    fun onDateSelected(day:Int, month: Int, year: Int){
        var actMonth = month + 1
        txtdate.setText("$day/$actMonth/$year")
    }

    private fun agregarGasto() {
        val nombre = findViewById<EditText>(R.id.txtTipoServicio).text.toString()
        val fecha = findViewById<EditText>(R.id.txtFechaServicio).text.toString()
        val total = findViewById<EditText>(R.id.txtTotalServicio).text.toString()

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
                dia = fecha,
                total = total.toFloat(),
                categoria = "Servicios"
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

    private fun agregarIdGastoLocal(gastoId: String,nombre : String,email : String){
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

    private fun abrirRecyclerView(gastos: List<String>) {
        // Abrir la actividad del RecyclerView y pasar la lista de gastos
        val intent = Intent(this@SeccionServicios, RecyclerServicios::class.java)
        intent.putStringArrayListExtra("listaGastos", ArrayList(gastos))
        startActivity(intent)
    }
    private fun modificarGasto(){
        val nombre = findViewById<EditText>(R.id.txtTipoServicio).text.toString()
        val fecha = findViewById<EditText>(R.id.txtFechaServicio).text.toString()
        val total = findViewById<EditText>(R.id.txtTotalServicio).text.toString()

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

        bd.child(localId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && result.exists()) {
                    val actualizaciones = mapOf<String, Any>(
                        "dia" to fecha,
                        "total" to total.toFloat(),
                    )

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

    private fun consultarGastos(){
        bd.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gastos = mutableListOf<String>()
                val result = task.result
                if (result != null && result.hasChildren()) { // Verifica si existen hijos
                    for (snapshot in result.children) {
                        val gasto = snapshot.getValue(Gasto::class.java)
                        if (gasto != null && gasto.categoria == "Servicios") {
                            val detalleGasto = "${gasto.nomGasto}: ${gasto.dia} - ${gasto.total}"
                            gastos.add(detalleGasto)
                        }
                    }
                    // Llama a abrirRecyclerView con los datos obtenidos
                    if(gastos.isEmpty()){
                        Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                    }else{
                        abrirRecyclerView(gastos)
                    }
                } else {
                    Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al cargar los gastos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun resetFields(){
        txtServicio.setText("")
        txtdate.setText("")
        txtcosto.setText("")
        txtServicio.requestFocus()
    }
    override fun onDestroy() {
        super.onDestroy()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("usuarios").child(userId).child("logged")

            // Actualizar el estado en la base de datos
            userRef.setValue(false)
                .addOnSuccessListener {
                    println("El estado de 'logged' se ha actualizado a false al cerrar la aplicación.")
                }
                .addOnFailureListener {
                    println("Error al actualizar el estado al cerrar la aplicación.")
                }
        }
    }
}