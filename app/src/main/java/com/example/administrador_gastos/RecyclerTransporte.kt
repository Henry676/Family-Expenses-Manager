package com.example.administrador_gastos

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RecyclerTransporte : AppCompatActivity() {
    private lateinit var miRecyclerView: RecyclerView
    private lateinit var miAdapter: AdaptadorTransporte
    private lateinit var miLayoutManager: RecyclerView.LayoutManager
    private lateinit var bd : DatabaseReference
    private lateinit var dbAdmin : DBController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_transporte)
        bd = FirebaseDB.databaseReference.child("gastos")
        dbAdmin = DBController(this, "gastosCasa.db", null, 1)


        // Obtener la lista de gastos desde el intent
        val listaGastos = intent.getStringArrayListExtra("listaGastos")

        miRecyclerView = findViewById(R.id.recyclerViewTransporte)
        miLayoutManager = LinearLayoutManager(this)
        miRecyclerView.layoutManager = miLayoutManager
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email

        // Crear y asignar el adaptador con la lista de gastos
        miAdapter = AdaptadorTransporte(
            listaGastos ?: ArrayList(),
            R.layout.recycler_view_item,
            object : AdaptadorTransporte.OnItemClickListener {
                override fun onItemClick(name: String?, position: Int) {
                    val  alert = AlertDialog.Builder(this@RecyclerTransporte)
                    alert.setMessage("¿Esta seguro de querer eliminar este gasto?")
                    alert.setTitle("Alerta")
                    alert.setCancelable(false)
                    alert.setPositiveButton("Si") {_,_ ->

                        val nombre = name?.split(":")?.get(0) ?: "" // Devuelve una cadena vacía si name es null
                        val db = dbAdmin.readableDatabase

                        val row = db.rawQuery(
                            "SELECT id, email FROM gastosFamiliares WHERE name = ? AND email = ?",
                            arrayOf(nombre,emailUsuarioLogueado), // Pasar el valor como parámetro
                        )
                        var localId = ""
                        var emailLocal = ""
                        if (row.moveToFirst()) {
                            localId = row.getString(0)
                            emailLocal = row.getString(1)
                        }
                        eliminarGasto(bd,localId,emailLocal,nombre,db)
                    }
                    alert.setNegativeButton("No"){_,_->
                        Toast.makeText(this@RecyclerTransporte, "Operación abortada",Toast.LENGTH_LONG).show()
                    }
                    alert.setNeutralButton("Cancelar"){_,_->
                        Toast.makeText(this@RecyclerTransporte, "Operación cancelada",Toast.LENGTH_LONG).show()
                    }
                    val dialogoAlerta = alert.create()
                    dialogoAlerta.show()
                }
            })
        miRecyclerView.adapter = miAdapter
    }
    private fun eliminarGasto(bd: DatabaseReference, gastoId: String, emailLocal: String,nombre : String, db: SQLiteDatabase) {
        bd.child(gastoId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val email = snapshot.child("email").value.toString()
                if (email == emailLocal) {
                    bd.child(gastoId).removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            db.execSQL("DELETE FROM gastosFamiliares WHERE id = ?", arrayOf(gastoId))
                            Toast.makeText(this, "Gasto eliminado de Firebase y del celular", Toast.LENGTH_SHORT).show()
                            actualizarListaYAdaptador(nombre)
                        } else {
                            Toast.makeText(this, "Error al eliminar el gasto", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El email no coincide. No se puede eliminar el gasto.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se encontró el gasto con ID: $gastoId", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al consultar el gasto: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarListaYAdaptador(nombreGasto: String) {
        // Primero, obtenemos la lista de gastos desde el adaptador
        val listaGastos = (miRecyclerView.adapter as AdaptadorTransporte).dato

        // Filtramos el gasto a eliminar de la lista local
        val gastoEliminado = listaGastos.find { it.split(":")[0] == nombreGasto }
        if (gastoEliminado != null) {
            // Eliminar el gasto de la lista
            listaGastos.remove(gastoEliminado)
            if(listaGastos.size == 0) finish()
            // Notificar al adaptador que los datos han cambiado
            miAdapter.notifyDataSetChanged()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_borrar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_name -> {
                Toast.makeText(this, "Para eliminar un registro, haga click en uno", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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