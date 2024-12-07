package com.example.administrador_gastos

import java.util.Date

data class ProximoPago(
    var id: String = "",
    var email: String = "",
    var nomPago: String = "",
    var dia: String = "",
    var total: Float = 0.0F,
    var horaAviso: String = ""
)