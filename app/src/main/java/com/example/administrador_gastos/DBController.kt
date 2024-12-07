package com.example.administrador_gastos


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.sql.SQLException

class DBController (
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int):
    SQLiteOpenHelper(context,name,factory,version
    ){

    override fun onCreate(db: SQLiteDatabase) {
        //Instruccion DDL (create)
        val sql = "CREATE TABLE gastosFamiliares (id TEXT PRIMARY KEY, name TEXT, email TEXT)"
        //Creacion de la BD
        try {
            db.execSQL(sql)
        } catch(e: SQLException){
            Toast.makeText(
                null,"Error al crear la base de datos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}


}