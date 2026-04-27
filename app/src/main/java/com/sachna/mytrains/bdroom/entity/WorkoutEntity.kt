package com.sachna.mytrains.bdroom.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workout_stats")
data class WorkoutEntity(
    @PrimaryKey val firebaseId: String,
    val exerciseName: String,
    val coachComment: String,
    val targetSeries: String,
    val targetReps: String,
    val targetRIR: String,
    val targetRest: String,

    // Datos introducidos por el usuario
    val weight: String,
    val repRealizada: String,
    val repPosible: String,
    val userNotes: String,


    val nomSesion: String,
    val numSemana: Int,
    val grupo_muscular: String,
    val date: Date, // Usaremos un conversor para esto
    val isCompleted: Boolean = false,
    val imageRes: Int
)
