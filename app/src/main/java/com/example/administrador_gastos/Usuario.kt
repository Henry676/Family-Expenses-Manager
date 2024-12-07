package com.example.administrador_gastos

data class Usuario(
    var logged: Boolean = false,
    var nombre: String = "",
    var usuario: String = "",
    var passwd: String = "",
    var email: String = "",
    var genero: String = ""
)
