package com.sachna.mytrains.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.sachna.mytrains.R


data class GymCenter(val nombre: String, val posicion: LatLng, val telefono: String, val direccion: String)

class GimnasiosFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val listaGimnasios = listOf(
        GymCenter("TrainsGym", LatLng(39.4680, -0.3765), "+34912345678", "Calle Ruzafa, 14, Valencia"),
        GymCenter("TrainsGym", LatLng(39.4657654, -0.3817825), "+34912999000", "Plaza España, 20, Valencia"),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gimnasios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment?
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_container, it).commit()
            }
        mapFragment.getMapAsync(this)

        configurarBusqueda()
        configurarBotonCercano()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        } catch (e: Exception) { Log.e("MAP", "Error estilo") }

        val builder = LatLngBounds.Builder()

        listaGimnasios.forEach { gym ->
            googleMap.addMarker(MarkerOptions()
                .position(gym.posicion)
                .title(gym.nombre)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
            builder.include(gym.posicion)
        }


        if (listaGimnasios.isNotEmpty()) {
            mostrarTarjetaInfo(listaGimnasios[0], moverCamara = false)
        }


        view?.post {
            val bounds = builder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        }

        googleMap.setOnMarkerClickListener { marker ->
            val gym = listaGimnasios.find { it.nombre == marker.title }
            gym?.let { mostrarTarjetaInfo(it, moverCamara = true) }
            true
        }
    }

    // --- LÓGICA DE BÚSQUEDA FILTRADA ---
    private fun configurarBusqueda() {
        val etSearch = view?.findViewById<EditText>(R.id.etSearchGym)
        etSearch?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()


                val encontrado = listaGimnasios.find {
                    it.nombre.contains(query, ignoreCase = true) || it.direccion.contains(query, ignoreCase = true)
                }

                if (encontrado != null) {
                    mostrarTarjetaInfo(encontrado, moverCamara = true)
                } else {
                    Toast.makeText(requireContext(), "Centro no encontrado en esta ubicación", Toast.LENGTH_SHORT).show()
                }

                // Ocultar teclado
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else false
        }
    }

    private fun configurarBotonCercano() {
        view?.findViewById<Button>(R.id.btnFindClosest)?.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { moverAlMasCercano(LatLng(it.latitude, it.longitude)) }
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
        }
    }

    private fun moverAlMasCercano(miPos: LatLng) {
        val masCercano = listaGimnasios.minByOrNull { gym ->
            val resultados = FloatArray(1)
            Location.distanceBetween(miPos.latitude, miPos.longitude, gym.posicion.latitude, gym.posicion.longitude, resultados)
            resultados[0]
        }
        masCercano?.let { mostrarTarjetaInfo(it, moverCamara = true) }
    }

    private fun mostrarTarjetaInfo(gym: GymCenter, moverCamara: Boolean) {
        val card = view?.findViewById<View>(R.id.cardContactInfo)
        val tvName = view?.findViewById<TextView>(R.id.tvGymName)
        val tvAddress = view?.findViewById<TextView>(R.id.tvGymAddress)
        val btnDirections = view?.findViewById<Button>(R.id.btnDirections)
        val btnCall = view?.findViewById<ImageButton>(R.id.btnCall)

        tvName?.text = gym.nombre
        tvAddress?.text = gym.direccion
        card?.visibility = View.VISIBLE

        if (moverCamara) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gym.posicion, 17f))
        }

        // Botón Cómo Llegar
        btnDirections?.setOnClickListener {
            val uri = "google.navigation:q=${gym.posicion.latitude},${gym.posicion.longitude}".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/dir/?api=1&destination=${gym.posicion.latitude},${gym.posicion.longitude}".toUri()))
            }
        }

        // Botón Llamar
        btnCall?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, "tel:${gym.telefono}".toUri())
            startActivity(intent)
        }
    }
}