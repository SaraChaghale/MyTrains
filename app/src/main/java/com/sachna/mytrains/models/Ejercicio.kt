package com.sachna.mytrains.models

import com.google.firebase.Timestamp

data class Ejercicio(
    var id: String = "",
    val nombre: String = "",
    val comentario_entrenador: String = "",
    val series: Int = 0,
    val rangoReps: Int = 0,
    val rir: Int = 0,
    val rest: Int = 0,
    val grupo_muscular: String = "",
    var pesoRealizado: Double = 0.0,
    var repsRealizadas: Int = 0,
    var repsPosibles: Int = 0,
    var comentario_usuario: String = "",
    var completado: Boolean = false,
    var nombreSesion: String = "",
    var parentPath: String = "",
    var fecha: com.google.firebase.Timestamp? = null,
    var fecha_realizado: com.google.firebase.Timestamp? = null
)
