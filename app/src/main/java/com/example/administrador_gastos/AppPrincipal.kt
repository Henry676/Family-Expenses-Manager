package com.example.administrador_gastos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AppPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_principal)

        window.statusBarColor = ContextCompat.getColor(this, R.color.cyan)// Color de status bar
        supportActionBar?.setBackgroundDrawable(// Color de appBar
            ContextCompat.getDrawable(this, R.color.cyan)
        )
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.cyan))// Color de navigation bar

        var seccion1: ImageButton
        var seccion2: ImageButton
        var seccion3: ImageButton
        var seccion4: ImageButton
        var seccion5: ImageButton
        var seccion6: ImageButton


        seccion1=findViewById(R.id.btnextras)
        seccion2=findViewById(R.id.btntransporte)
        seccion3=findViewById(R.id.btncomida)
        seccion4=findViewById(R.id.btnservicios)
        seccion5=findViewById(R.id.btnhistorial)
        seccion6=findViewById(R.id.btnpagosprox)

        seccion1.setOnClickListener{
            val intent = Intent(this@AppPrincipal, SeccionExtras::class.java)
            startActivity(intent)
        }

        seccion2.setOnClickListener{
            val intent = Intent(this@AppPrincipal, SeccionTransporte::class.java)
            startActivity(intent)
        }

        seccion3.setOnClickListener{
            val intent = Intent(this@AppPrincipal, SeccionComida::class.java)
            startActivity(intent)
        }

        seccion4.setOnClickListener{
            val intent = Intent(this@AppPrincipal, SeccionServicios::class.java)
            startActivity(intent)

        }

        seccion5.setOnClickListener{
            val intent = Intent(this@AppPrincipal, Historial::class.java)
            startActivity(intent)
        }
        seccion6.setOnClickListener{
            val intent = Intent(this@AppPrincipal, SeccionProximosPagos::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.foto -> {
                val intent = Intent(this,FotoTicket::class.java)
                startActivity(intent)
                return true
            }

            R.id.ubicacion -> {
                val intent = Intent(this,MapsActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Mostrando ubicación de la casa", Toast.LENGTH_LONG).show()
                return true
            }

            R.id.cuenta -> {
                val intent = Intent(this,FormularioRegistro::class.java)
                startActivity(intent)
                return true
            }

            R.id.cerrarSesion -> {

                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser

                if (user != null) {
                    val userId = user.uid
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference("usuarios").child(userId).child("logged")

                    // Actualizar el estado en la base de datos
                    userRef.setValue(false)
                        .addOnSuccessListener {
                            println("El estado de 'logged' se ha actualizado a false.")
                            // Cerrar sesión en Firebase Authentication
                            auth.signOut()
                            val intent = Intent(this,Login::class.java)
                            startActivity(intent)
                            finish()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No hay un usuario autenticado", Toast.LENGTH_SHORT).show()
                }
                return true
            }


            else -> return super.onOptionsItemSelected(item)
        }
    }

}