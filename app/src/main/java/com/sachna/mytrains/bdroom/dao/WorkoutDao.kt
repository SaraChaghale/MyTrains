package com.sachna.mytrains.bdroom.dao

import androidx.room.*
import com.sachna.mytrains.bdroom.entity.WorkoutEntity
import java.util.Date

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity):Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workout_stats WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getAllWorkoutsOnce(): List<WorkoutEntity>
    // Obtener ejercicios entre dos fechas (útil para día, semana o mes)
    @Query("SELECT * FROM workout_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(startDate: Date, endDate: Date): List<WorkoutEntity>

    @Query("SELECT COUNT(DISTINCT nomSesion) FROM workout_stats WHERE isCompleted = 1")
    suspend fun countCompletedSessions(): Int

    @Query("SELECT COUNT(DISTINCT numSemana) FROM workout_stats WHERE isCompleted = 1")
    suspend fun countCompletedWeeks(): Int

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}