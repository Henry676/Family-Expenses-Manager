package com.example.administrador_gastos

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
object FirebaseDB {
    val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}