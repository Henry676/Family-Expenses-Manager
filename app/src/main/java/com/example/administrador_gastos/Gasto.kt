package com.example.administrador_gastos


data class Gasto(
    var id: String = "",
    var email: String = "",
    var nomGasto: String = "",
    var cantidad: Int = 0,
    var dia: String = "",
    var total: Float = 0.0F,
    var categoria: String = "",
)
