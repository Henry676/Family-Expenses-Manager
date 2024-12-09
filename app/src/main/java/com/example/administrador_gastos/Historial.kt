package com.example.administrador_gastos

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Historial : AppCompatActivity() {

    private lateinit var miRecyclerView: RecyclerView
    private lateinit var miAdapter: AdaptadorHistorial
    private lateinit var miLayoutManager: RecyclerView.LayoutManager
    private lateinit var buscadorGastos: AutoCompleteTextView
    private lateinit var gastosPorCategorias: Spinner

    private lateinit var dataBase : DatabaseReference

    private lateinit var dbAdmin : DBController


    private val currentList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        dataBase = FirebaseDatabase.getInstance().getReference("gastos")

        dbAdmin = DBController(this, "gastosCasa.db", null, 1)
        /*val db = dbAdmin.readableDatabase
        db.delete("gastosFamiliares", null, null) // Sin WHERE para borrar all
        db.close()*/


        window.statusBarColor = ContextCompat.getColor(this, R.color.cyan)// Color de status bar
        supportActionBar?.setBackgroundDrawable(// Color de appBar
            ContextCompat.getDrawable(this, R.color.cyan)
        )
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.cyan))// Color de navigation bar

        buscadorGastos = findViewById(R.id.buscador)
        gastosPorCategorias = findViewById(R.id.spinner_registro)

        val categorias = listOf("Servicios", "Extras", "Comida", "Transporte")

        // Configurar el AutoCompleteTextView
        ArrayAdapter(this, android.R.layout.simple_list_item_1, categorias).also { adapter ->
            buscadorGastos.setAdapter(adapter)
        }

        // Configurar el Spinner
        ArrayAdapter(this, android.R.layout.simple_list_item_1, categorias).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gastosPorCategorias.adapter = adapter
        }

        // Configurar RecyclerView
        miRecyclerView = findViewById(R.id.recyclerView)
        miLayoutManager = LinearLayoutManager(this)
        miAdapter = AdaptadorHistorial(currentList, object : AdaptadorHistorial.OnItemClickListener {
            override fun onItemClick(position: Int) {
                confirmDelete(currentList[position])
            }
        })
        miRecyclerView.layoutManager = miLayoutManager
        miRecyclerView.adapter = miAdapter

        // Manejar selección del spinner
        gastosPorCategorias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categorias[position]
                consultarGastos(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        buscadorGastos.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    obtenerNombresGastos(query)
                }
            }
        })
    }

    // Función para obtener los nombres de los gastos desde Firebase
    private fun obtenerNombresGastos(busqueda: String) {
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        dataBase.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gastos = mutableListOf<String>()
                val result = task.result
                if (result != null && result.hasChildren()) {
                    for (snapshot in result.children) {
                        val gasto = snapshot.getValue(Gasto::class.java)
                        if (gasto != null && gasto.nomGasto.contains(busqueda, ignoreCase = true) && gasto.email == emailUsuarioLogueado) {
                            gastos.add(gasto.nomGasto)
                        }
                    }
                    // Actualizar las sugerencias en el AutoCompleteTextView
                    actualizarSugerencias(busqueda, gastos)
                }
            } else {
                Toast.makeText(this, "Error al cargar los gastos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarSugerencias(busqueda: String, gastos: List<String>) {
        val filteredGastos = gastos.filter { it.contains(busqueda, ignoreCase = true) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredGastos)
        buscadorGastos.setAdapter(adapter)
    }

    // Función para consultar los gastos de una categoría
    private fun consultarGastos(category: String) {
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        dataBase.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gastos = mutableListOf<String>()
                val result = task.result
                if (result != null && result.hasChildren()) {
                    for (snapshot in result.children) {
                        val gasto = snapshot.getValue(Gasto::class.java)
                        if (gasto != null && gasto.categoria == category && gasto.email == emailUsuarioLogueado) {
                            val detalleGasto = "${gasto.nomGasto}: ${gasto.cantidad} - ${gasto.dia} - ${gasto.total}"
                            gastos.add(detalleGasto)
                        }
                    }
                    // Actualizar RecyclerView con los datos filtrados
                    miAdapter.updateData(gastos)
                } else {
                    Toast.makeText(this, "No hay gastos registrados en la categoría $category", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al cargar los gastos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para confirmar la eliminación de un gasto
    private fun confirmDelete(item: String) {
        val emailUsuarioLogueado = FirebaseAuth.getInstance().currentUser?.email
        if (emailUsuarioLogueado == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("¿Está seguro de querer eliminar este registro?")
            .setTitle("Confirmar eliminación")
            .setPositiveButton("Sí") { _, _ ->
                val db = dbAdmin.readableDatabase
                val nombre = item.split(":")[0]

                val row = db.rawQuery(
                    "SELECT id FROM gastosFamiliares WHERE name = ? AND email = ?",
                    arrayOf(nombre,emailUsuarioLogueado), // Pasar el valor como parámetro
                )
                var localId = ""
                if (row.moveToFirst()) {
                    localId = row.getString(0)
                }
                eliminarGasto(localId,nombre,dataBase,db)
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Función para eliminar un gasto de Firebase
    private fun eliminarGasto(gastoId: String, nombre : String, bd: DatabaseReference, db: SQLiteDatabase) {
        bd.child(gastoId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.execSQL("DELETE FROM gastosFamiliares WHERE id = ?", arrayOf(gastoId))
                Toast.makeText(this, "Gasto eliminado de Firebase", Toast.LENGTH_SHORT).show()
                actualizarListaYAdaptador(nombre)
            } else {
                Toast.makeText(this, "Error al eliminar el gasto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para actualizar la lista y el adaptador después de eliminar un gasto
    private fun actualizarListaYAdaptador(nombre: String) {
        val listaGastos = (miRecyclerView.adapter as AdaptadorHistorial).items
        //val gastoEliminado = listaGastos.find { it.split(":")[0] == nombre }
        if (nombre != null) {
            listaGastos.remove(nombre)
            if (listaGastos.size == 0) finish()
            miAdapter.notifyDataSetChanged()
        }
    }

    // Inflar el menú
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
}
