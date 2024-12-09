package com.example.administrador_gastos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class Login : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var usuarioIcon: ImageView
    private lateinit var toggleButton: ToggleButton
    private lateinit var usuarioET: EditText
    private lateinit var contraET: EditText
    private lateinit var recordarContra: CheckBox
    private lateinit var restablePaswd: TextView
    private lateinit var cuenta: TextView
    private lateinit var bd: DatabaseReference
    private lateinit var boton: Button
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bd = FirebaseDatabase.getInstance().getReference("usuarios")
        
        imageView = findViewById(R.id.imageView2)
        usuarioET = findViewById(R.id.campoUsuario)
        contraET = findViewById(R.id.campoContraseña)
        usuarioIcon = findViewById(R.id.user)
        toggleButton = findViewById(R.id.toggleButton)
        recordarContra = findViewById(R.id.recordarPasswd)
        
        // Inicializa FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Inicializar las vistas
        boton = findViewById(R.id.iniciarSesion)
        var campos = true

        boton.setOnClickListener {

            usuarioET.error = null
            contraET.error = null

            if (usuarioET.text.toString().isEmpty()){
                campos = false
                usuarioET.error = "Campo requerido"
            }
            if (contraET.text.toString().isEmpty()){
                campos = false
                contraET.error = "Campo requerido"
            }
            if(campos) {
                verificarUsuario(usuarioET.text.toString(), contraET.text.toString())
            }
        }

        var link: TextView = findViewById(R.id.crearCuenta)
        var link2: TextView = findViewById(R.id.restablecerContr)

        link.setOnClickListener {
            val intent = Intent(this@Login, FormularioRegistro::class.java)
            startActivity(intent)
        }

        link2.setOnClickListener {
            val email = usuarioET.text.toString().trim()

            // Verifica que el campo de correo no esté vacío
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese un correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llama a la función para enviar el correo de restablecimiento
            enviarCorreoRestablecimiento(email)
        }



        // Restaurar el estado del ToggleButton
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        toggleButton.isChecked = isDarkMode

        // Aplicar el modo oscuro o claro según el estado restaurado
        applyDarkMode(isDarkMode)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("DarkMode", isChecked)
            editor.apply()

            // Aplicar el modo oscuro o claro
            applyDarkMode(isChecked)
        }

    }
    private fun enviarCorreoRestablecimiento(email: String) {
        // Verificar si el correo existe en la base de datos
        bd.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Si el correo existe, enviamos el correo de restablecimiento
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(this@Login) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@Login, "Correo de restablecimiento enviado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@Login, "Hubo un error al enviar el correo", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Si no existe el correo, mostramos un mensaje
                    Toast.makeText(this@Login, "No existe el usuario con dicho correo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Función para verificar usuario y contraseña en Firebase
    private fun verificarUsuario(input: String, password: String) {
        // Intenta iniciar sesión directamente con el correo electrónico
        auth.signInWithEmailAndPassword(input, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleSuccessfulLogin(auth.currentUser, password)
                } else {
                    // Si falla, intenta buscar el usuario por nombre en la base de datos
                    buscarPorNombreDeUsuario(input, password)
                }
            }
    }

    private fun buscarPorNombreDeUsuario(username: String, password: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("usuarios")
        userRef.orderByChild("usuario").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.children.firstOrNull()?.getValue(Usuario::class.java)
                        if (user != null && user.email.isNotEmpty()) {
                            // Intenta iniciar sesión con el correo recuperado
                            auth.signInWithEmailAndPassword(user.email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        handleSuccessfulLogin(auth.currentUser, password)
                                    } else {
                                        Toast.makeText(
                                            this@Login,
                                            "Usuario o contraseña incorrecta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this@Login, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Login, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Login, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleSuccessfulLogin(currentUser: FirebaseUser?, password: String) {
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("logged")

            userRef.setValue(true)
                .addOnSuccessListener {
                    // Navegar a la pantalla principal
                    val intent = Intent(this@Login, AppPrincipal::class.java).apply {
                        putExtra("USER_EMAIL", currentUser.email)
                    }
                    Toast.makeText(this, "Iniciando sesión", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar estado de sesión", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    // Función para verificar la contraseña
    /*private fun verificarPasswd(user: Usuario?, contra: String) {
        if (user != null && user.passwd == contra) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userEmail = currentUser.email
                val userId = currentUser.uid

                // Actualiza el estado del usuario logeado en la base de datos
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("usuarios").child(userId).child("logged")

                userRef.setValue(true)
                    .addOnSuccessListener {
                        println("El estado de 'logged' se ha actualizado a true.")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar el estado de 'logged'.", Toast.LENGTH_SHORT).show()
                    }

                // Navega a la pantalla principal con los datos del usuario autenticado
                val intent = Intent(this@Login, AppPrincipal::class.java).apply {
                    putExtra("USER_EMAIL", userEmail) // Envía el correo al siguiente Activity
                }
                startActivity(intent)
                resetFields()
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun resetFields(){
        usuarioET.setText("")
        contraET.setText("")
        usuarioET.requestFocus()
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color_dark)
            imageView.setImageResource(R.drawable.darkmode)
            usuarioIcon.setImageResource(R.drawable.usuario)
            findViewById<TextView>(R.id.crearCuenta).setTextColor(ContextCompat.getColor(this, R.color.cyan))
            findViewById<TextView>(R.id.restablecerContr).setTextColor(ContextCompat.getColor(this, R.color.cyan))
            usuarioET.setTextColor(ContextCompat.getColor(this, R.color.white))
            contraET.setTextColor(ContextCompat.getColor(this, R.color.white))
            usuarioET.setHintTextColor(ContextCompat.getColor(this, R.color.white))
            contraET.setHintTextColor(ContextCompat.getColor(this, R.color.white))
            boton.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan))
            boton.setTextColor(ContextCompat.getColor(this, R.color.black))
            recordarContra.setTextColor(ContextCompat.getColor(this, R.color.cyan))
            recordarContra.buttonDrawable = ContextCompat.getDrawable(this, R.drawable.check_selector_dark)  // Contorno claro
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color_light)
            imageView.setImageResource(R.mipmap.fondo)
            usuarioIcon.setImageResource(R.mipmap.icon)
            findViewById<TextView>(R.id.crearCuenta).setTextColor(ContextCompat.getColor(this, R.color.white))
            findViewById<TextView>(R.id.restablecerContr).setTextColor(ContextCompat.getColor(this, R.color.white))
            usuarioET.setTextColor(ContextCompat.getColor(this, R.color.black))
            contraET.setTextColor(ContextCompat.getColor(this, R.color.black))
            usuarioET.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            contraET.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            boton.setBackgroundColor(ContextCompat.getColor(this, R.color.black))  // Cambiar fondo del botón
            boton.setTextColor(ContextCompat.getColor(this, R.color.white))
            recordarContra.setTextColor(ContextCompat.getColor(this, R.color.white))
            recordarContra.buttonDrawable = ContextCompat.getDrawable(this, R.drawable.check_selector_light)  // Contorno claro
        }
    }
}
