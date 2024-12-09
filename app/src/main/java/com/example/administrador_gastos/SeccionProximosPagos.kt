package com.example.administrador_gastos

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.sql.SQLException
import java.util.Calendar

class SeccionProximosPagos : AppCompatActivity() {
    //private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var bd : DatabaseReference
    private val idCanal: String = "Canal01"
    private lateinit var notificacion: NotificationCompat.Builder
    private lateinit var btnNotificacion: Button
    private lateinit var btnMostrar: Button
    private lateinit var btnCancelar: Button

    private lateinit var nombrePago: EditText
    private lateinit var pagoProximo: EditText
    private lateinit var horaPagar: EditText
    private lateinit var total: EditText

    private lateinit var dbAdmin : DBController

    private var dia: Int = 0
    private var mes: Int = 0
    private var año: Int = 0
    private var hora: Int = 0
    private var minuto: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seccion_proximos_pagos)
        bd = FirebaseDB.databaseReference.child("pagos")
        dbAdmin = DBController(this, "gastosCasa.db", null, 1)

        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color_dark)

        btnNotificacion = findViewById(R.id.establecerPago)
        btnMostrar = findViewById(R.id.showRecyclerProximos)
        btnCancelar = findViewById(R.id.cancelarProximos)

        nombrePago = findViewById(R.id.txtNombreProximoPago)
        pagoProximo = findViewById(R.id.txtFechaProximo)
        horaPagar = findViewById(R.id.txtHoraProximo)
        total = findViewById(R.id.txtTotalProximo)

        crearCanalDeNotificacion()

        pagoProximo.setOnClickListener {
            obtenerFechaAlarma()
        }
        horaPagar.setOnClickListener {
            obtenerHoraAlarma()
        }
        btnNotificacion.setOnClickListener {
            if(validarCampos()){
                agregarPago()
            }else{
                return@setOnClickListener
            }
        }

        btnMostrar.setOnClickListener {
            consultarPagos()
        }

        btnCancelar.setOnClickListener {
            val pagoID = nombrePago.text.toString().trim()

            if (pagoID.isEmpty()) {
                Toast.makeText(this, "Agrega el nombre del pago para cancelar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verificarYEliminarPago()
        }
    }
    private fun obtenerFechaAlarma(){
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{ view, year, monthOfYear, dayOfMonth ->
            dia = dayOfMonth
            mes = (monthOfYear+1)
            año = year
            pagoProximo.setText("$dia/$mes/$año")
        },year,month,day) //Parametros para que el datepicker me cargue con los datos actuales
        dpd.show()

    }

    private fun obtenerHoraAlarma(){
        val c: android.icu.util.Calendar = android.icu.util.Calendar.getInstance()
        val hour: Int = c.get(android.icu.util.Calendar.HOUR_OF_DAY)
        val minute: Int = c.get(android.icu.util.Calendar.MINUTE)
        val tpd = TimePickerDialog(this,TimePickerDialog.OnTimeSetListener(
            function = {view,hr,mn ->
                hora = hr
                minuto = mn
                horaPagar.setText(String.format("%02d:%02d", hora, minuto))
            }),hour,minute,true)
        tpd.show()
    }
    private fun agregarPago(){
        val nombre = nombrePago.text.toString()
        val fecha = pagoProximo.text.toString()
        val hora = horaPagar.text.toString()
        val totalMonto = total.text.toString()

        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            throw IllegalStateException("Usuario no autenticado")
        }
        val pagoId = bd.push().key

        // Crear objeto de pago para subir a Firebase
        if (pagoId != null) {
            agregarIdGastoLocal(pagoId,nombre,emailUsuarioLogueado)


            val proximoPago = ProximoPago(
                id = pagoId,
                email = emailUsuarioLogueado,
                nomPago = nombre,
                dia = fecha,
                total = totalMonto.toFloat(),
                horaAviso = hora
            )

            bd.child(pagoId).setValue(proximoPago).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pago registrado exitosamente", Toast.LENGTH_SHORT).show()
                    agendarNotificacion(nombre,totalMonto)
                    crearNotificacion()
                    //enviarNotificacion()
                    resetFields()
                } else {
                    Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun validarCampos() : Boolean{
        var campos = true

        if (nombrePago.text.toString().isEmpty()){
            campos = false
            nombrePago.error = "Campo requerido"
        }
        if (pagoProximo.text.toString().isEmpty()){
            campos = false
            pagoProximo.error = "Campo requerido"
        }
        if (horaPagar.text.toString().isEmpty()){
            campos = false
            horaPagar.error = "Campo requerido"
        }
        return campos
    }
    private fun agregarIdGastoLocal(pagoId: String,nombre: String,email: String){
        val db = dbAdmin.writableDatabase
        val record = ContentValues()
        record.put("id", pagoId)
        record.put("name", nombre)
        record.put("email",email)
        if(db != null){
            // Almacenar valores en la tabla
            try{
                val x: Long = db.insert("gastosFamiliares",null,record)
                Toast.makeText(this,"Registro guardado de manera local",Toast.LENGTH_SHORT).show()
            } catch(e: SQLException){
                Log.e("Exception","Error: " + e.message.toString())
            }
            db.close()
        }
    }


    private fun verificarYEliminarPago() {
        val nombrePago = nombrePago.text.toString()
        val db = dbAdmin.writableDatabase
        val dbR = dbAdmin.readableDatabase
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email


        val row = dbR.rawQuery(
            "SELECT id FROM gastosFamiliares WHERE name = ? AND email = ?",
            arrayOf(nombrePago,emailUsuarioLogueado), // Pasar el valor como parámetro
        )
        var localId = ""
        if (row.moveToFirst()) {
            localId = row.getString(0)
        }
        bd.child(localId).removeValue()
            .addOnSuccessListener {
                db.execSQL("DELETE FROM gastosFamiliares WHERE id = ?", arrayOf(localId))
                Toast.makeText(this, "Gasto cancelado.", Toast.LENGTH_SHORT).show()
                resetFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cancelar gasto.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun crearNotificacion(){
        //Creacion de flag
        val intent = Intent(this, SeccionProximosPagos::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flag = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this,0,intent,flag)

        notificacion = NotificationCompat.Builder(this,idCanal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Pago programado")
            .setContentText("Se ha registrado un pago proximo")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El pago ${nombrePago.text.toString()} "+
                        "se ha programado exitosamente " +
                        "en la base de datos" +
                        "de firebase\n"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
    // funcion para enviar la notificacion
    /*@SuppressLint("MissingPermission") // Esta anotacion evita la advertencia del permiso que se necesita por parte del usuario
    private fun enviarNotificacion(){
        with(NotificationManagerCompat.from(this)){
            notify(1,notificacion.build())
        }
    }*/

    private fun resetFields(){
        nombrePago.setText("")
        pagoProximo.setText("")
        horaPagar.setText("")
        total.setText("")
    }
    private fun crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombreCanal = "Notificacion"
            val descripcionCanal = "Canal de notificacion para la app Administrador de gastos"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel(
                idCanal,
                nombreCanal,
                importancia
            ).apply {
                description = descripcionCanal
            }
            val adminDeNotificacion = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            adminDeNotificacion.createNotificationChannel(canal)
        }
    }

    private fun consultarPagos(){
        // Obtener el email del usuario logeado
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email

        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        bd.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val pagos = mutableListOf<String>()
                val result = task.result
                if (result != null && result.hasChildren()) { // Verifica si existen hijos
                    for (snapshot in result.children) {
                        val pago = snapshot.getValue(ProximoPago::class.java)

                        if (pago != null && pago.email == emailUsuarioLogueado) {
                            val detallePago = "${pago.nomPago}: ${pago.dia} - ${pago.total}"
                            pagos.add(detallePago)
                        }
                    }
                    // Llama a abrirRecyclerView con los datos obtenidos
                    if(pagos.isEmpty()){
                        Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                    }else{
                        abrirRecyclerView(pagos)
                    }                  } else {
                    Toast.makeText(this, "No hay gastos registrados", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al cargar los gastos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun abrirRecyclerView(pagos: List<String>) {
        val intent = Intent(this@SeccionProximosPagos, RecyclerProximos::class.java)
        intent.putStringArrayListExtra("listaPagos", ArrayList(pagos))
        startActivity(intent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun agendarNotificacion(nombre:String,totalMonto:String) {
        val intent = Intent(applicationContext, AlarmaReceiver::class.java).apply {
            putExtra("nombrePago", nombre)
            putExtra("total", totalMonto)
        }
        pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE)//Flag immutable es Para que la alarma solo se pueda establecer una vez
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,hora)
            set(Calendar.MINUTE,minuto)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setExact(//alarma programable, se activa exactamente a la prevista
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

}
