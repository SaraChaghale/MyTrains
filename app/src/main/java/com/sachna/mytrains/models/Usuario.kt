package com.sachna.mytrains.models

data class Usuario(
    val nombre: String = "",
    val apellido1: String = "",
    val apellido2: String = "",
    val correo: String = "",
    val dni: String = "",
    val objetivo: String = "",
    val telefono: Long = 0)
