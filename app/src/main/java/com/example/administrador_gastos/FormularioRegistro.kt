package com.example.administrador_gastos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FormularioRegistro : AppCompatActivity() {
    private lateinit var bd : DatabaseReference
    private lateinit var txtnombre: EditText
    private lateinit var txtusuario: EditText
    private lateinit var txtpassw: EditText
    private lateinit var txtemail: EditText
    private var genero = ""

    private lateinit var boton: Button
    private lateinit var boton2: Button
    private lateinit var auth: FirebaseAuth // FirebaseAuth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        bd = FirebaseDatabase.getInstance().getReference("usuarios")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_registro)

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky)// Color de status bar
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lowSky))// Color de navigation bar

        boton = findViewById(R.id.button2)
        boton2 = findViewById(R.id.button3)

        txtnombre = findViewById(R.id.txtnombre)
        txtusuario = findViewById(R.id.txtusuario)
        txtpassw = findViewById(R.id.txtpassw)
        txtemail = findViewById(R.id.txtemail)

        auth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth

        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        radioGroup.clearCheck()
        // Listener para radio group
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            genero = when (checkedId) {
                R.id.masc -> "Masculino"
                R.id.fem -> "Femenino"
                else -> ""
            }
        }

        boton.setOnClickListener {

            if (camposValidos()) {
                registrarUsuario()
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        boton2.setOnClickListener {

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            val userId = user!!.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("usuarios").child(userId).child("logged")

            // Lee el valor del nodo "logged"
            userRef.get().addOnSuccessListener { snapshot ->
                // Verifica si el valor de "logged" es true
                val logged = snapshot.getValue(Boolean::class.java) ?: false

                if (camposValidos() && logged) {
                    // Si los campos son válidos y el usuario está logueado
                    modificarUsuario()
                    finish()
                    resetFields()
                } else if(!logged){
                    Toast.makeText(this@FormularioRegistro, "Inicie sesión primero", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@FormularioRegistro, "Error al verificar el estado de sesión", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun camposValidos() : Boolean{
        var camposValidos = true

        txtnombre.error = null
        txtusuario.error = null
        txtpassw.error = null
        txtemail.error = null

        if (txtnombre.text.toString().isEmpty()) {
            camposValidos = false
            txtnombre.error = "Campo requerido"
        }
        if (txtusuario.text.toString().isEmpty()) {
            camposValidos = false
            txtusuario.error = "Campo requerido"
        }
        if (txtpassw.text.toString().isEmpty()) {
            camposValidos = false
            txtpassw.error = "Campo requerido"
        }
        if (txtemail.text.toString().isEmpty()) {
            camposValidos = false
            txtemail.error = "Campo requerido"
        }
        return camposValidos
    }
    private fun registrarUsuario(){
        // Crear el usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(txtemail.text.toString(), txtpassw.text.toString())
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Obtener el UID del usuario autenticado
                val user = auth.currentUser
                val userId = user?.uid

                // Si se ha creado el usuario exitosamente, guardamos los datos en Realtime Database
                if (userId != null) {
                    // Crear objeto Usuario usando data class
                    val usuario = Usuario(
                        nombre = txtnombre.text.toString(),
                        usuario = txtusuario.text.toString(),
                        passwd = txtpassw.text.toString(),
                        email = txtemail.text.toString(),
                        genero = genero
                    )

                    // Guardar usuario en Firebase Realtime Database
                    bd.child(userId).setValue(usuario)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al registrar usuario en base de datos", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                // Si hubo un error al crear el usuario en Firebase Authentication
                Toast.makeText(this, "Error al registrar usuario: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun modificarUsuario() {
        val nombre = findViewById<EditText>(R.id.txtnombre).text.toString()
        val email = findViewById<EditText>(R.id.txtemail).text.toString()
        val passwd = findViewById<EditText>(R.id.txtpassw).text.toString()
        val usuario = findViewById<EditText>(R.id.txtusuario).text.toString()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userId = user!!.uid

        // Crear un mapa con los campos que se desean actualizar
        val usuarioModificado = mapOf<String, Any>(
            "nombre" to nombre,
            "email" to email,
            "passwd" to passwd,
            "usuario" to usuario,
            "genero" to genero
        )

        // Usar updateChildren para actualizar solo los campos necesarios
        bd.child(userId).updateChildren(usuarioModificado).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Datos del usuario modificados", Toast.LENGTH_SHORT).show()
                resetFields()
            } else {
                Toast.makeText(this, "Error al modificar los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetFields(){
        txtnombre.setText("")
        txtusuario.setText("")
        txtpassw.setText("")
        txtemail.setText("")
        txtnombre.requestFocus()
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
