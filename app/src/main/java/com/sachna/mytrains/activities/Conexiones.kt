package com.sachna.mytrains.activities

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sachna.mytrains.models.Ejercicio
import java.util.Calendar

class Conexiones {
    // Las dos herramientas que necesitamos de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Intenta iniciar sesión con los servidores de Google.
     * Devuelve 'true' y el UID si el usuario es premium y existe.
     */
    fun login(email: String, pass: String, alTerminar: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    // Si la contraseña es correcta, obtenemos el ID único
                    val uid = auth.currentUser?.uid
                    alTerminar(true, uid)
                } else {
                    // Si falla, devolvemos false
                    alTerminar(false, null)
                }
            }
    }

    /**
     * PASO 1: Actualiza un ejercicio y dispara la revisión de la sesión.
     */
    fun actualizarEjercicioYSubir(uid: String, semanaId: String, sesionId: String, ejercicioId: String, completado: Boolean) {
        val sesionRef = db.collection("usuarios").document(uid)
            .collection("entrenamientos").document(semanaId)
            .collection("sesiones").document(sesionId)

        sesionRef.collection("ejercicios").document(ejercicioId)
            .update("completado", completado)
            .addOnSuccessListener {
                comprobarNivelSesion(sesionRef, uid, semanaId)
            }
    }

    /**
     * Obtiene todos los ejercicios NO completados de la semana actual (Lunes a Domingo).
     */
    fun obtenerPendientesSemanaActual(uid: String, onResult: (List<Ejercicio>) -> Unit) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY

        // Forzamos a que sea el lunes de ESTA semana
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // LIMPIEZA TOTAL: hora, minuto, segundo y MILISEGUNDO
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0) // <-- Esto es vital
        val inicioSemana = cal.time

        val calFin = Calendar.getInstance()
        calFin.time = inicioSemana
        calFin.add(Calendar.DATE, 6)
        calFin.set(Calendar.HOUR_OF_DAY, 23)
        calFin.set(Calendar.MINUTE, 59)
        calFin.set(Calendar.SECOND, 59)
        calFin.set(Calendar.MILLISECOND, 999) // <-- Cobertura total
        val finSemana = calFin.time

        db.collectionGroup("ejercicios")
            .whereEqualTo("completado", false)
            .whereGreaterThanOrEqualTo("fecha", inicioSemana)
            .whereLessThanOrEqualTo("fecha", finSemana)
            .get()
            .addOnSuccessListener { snapshot ->
                // LOG PARA DEPURAR:
                android.util.Log.d("FIRESTORE", "Docs encontrados: ${snapshot.size()}")

                val lista = snapshot.documents.mapNotNull { doc ->

                    if (doc.reference.path.contains(uid)) {
                        val ej = doc.toObject(Ejercicio::class.java)
                        ej?.id = doc.id
                        ej?.parentPath = doc.reference.path

                        val rutaPartes = doc.reference.path.split("/")
                        val indiceSesiones = rutaPartes.indexOf("sesiones")

                        if (indiceSesiones != -1 && indiceSesiones + 1 < rutaPartes.size) {

                            val idSesion = rutaPartes[indiceSesiones + 1]


                            ej?.nombreSesion = idSesion.replace("sesion", "SESIÓN ").uppercase()
                        }
                        ej
                    } else {
                        null
                    }
                }
                onResult(lista)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE", "Error: ${e.message}")
                onResult(emptyList())
            }
    }

    fun obtenerEjerciciosParaStats(uid: String, onResult: (List<Ejercicio>) -> Unit) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY


        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)


        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val inicioSemana = cal.time

        val calFin = Calendar.getInstance()
        calFin.time = inicioSemana
        calFin.add(Calendar.DATE, 6)


        calFin.set(Calendar.HOUR_OF_DAY, 23)
        calFin.set(Calendar.MINUTE, 59)
        calFin.set(Calendar.SECOND, 59)
        calFin.set(Calendar.MILLISECOND, 999)
        val finSemana = calFin.time

        db.collectionGroup("ejercicios")
            .whereGreaterThanOrEqualTo("fecha", inicioSemana)
            .whereLessThanOrEqualTo("fecha", finSemana)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { doc ->
                    if (doc.reference.path.contains(uid)) {
                        val ej = doc.toObject(Ejercicio::class.java)
                        ej?.id = doc.id
                        ej?.parentPath = doc.reference.path

                        val rutaPartes = doc.reference.path.split("/")
                        val indiceSesiones = rutaPartes.indexOf("sesiones")
                        if (indiceSesiones != -1 && indiceSesiones + 1 < rutaPartes.size) {

                            ej?.nombreSesion = rutaPartes[indiceSesiones + 1]
                        }
                        ej
                    } else null
                }
                onResult(lista.sortedBy { it.fecha })
            }
            .addOnFailureListener { e ->

                android.util.Log.e("FIRESTORE_ERROR", "Copia este link para crear el índice: ${e.message}")
                onResult(emptyList())
            }
    }


    private fun comprobarNivelSesion(sesionRef: DocumentReference, uid: String, semanaId: String) {
        sesionRef.collection("ejercicios").get().addOnSuccessListener { snapshot ->
            val todosEjerciciosHechos = snapshot.documents.all { it.getBoolean("completado") == true }


            sesionRef.update("completado", todosEjerciciosHechos).addOnSuccessListener {

                comprobarNivelSemana(uid, semanaId)
            }
        }
    }


    private fun comprobarNivelSemana(uid: String, semanaId: String) {
        val semanaRef = db.collection("usuarios").document(uid)
            .collection("entrenamientos").document(semanaId)

        semanaRef.collection("sesiones").get().addOnSuccessListener { snapshot ->

            val todasSesionesHechas = snapshot.documents.all { it.getBoolean("completado") == true }

            semanaRef.update("completado", todasSesionesHechas)
        }
    }

}

//class Conexiones {
    // Ejemplo conceptual de petición de permisos
//    val permissions = setOf(
//        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
//        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
//    )
//
//    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
//
//    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
//        if (granted.containsAll(permissions)) {
//            // Ya puedes leer los entrenos de GainsProject
//        }
//    }
//}