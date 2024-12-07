package com.example.administrador_gastos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MapsActivity : FragmentActivity(), OnMapReadyCallback,GoogleMap.OnMapClickListener {
    private lateinit var btnMapsMap: Button
    private lateinit var btnMapsTerrain: Button
    private lateinit var btnMapsHybrid: Button
    private lateinit var btnMapsPolylines: Button
    private lateinit var mMap: GoogleMap
    private var minimumDistance = 1
    private val PERMISSION_LOCATION = 999
    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment : SupportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1000
            smallestDisplacement = minimumDistance.toFloat()
        }
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult : LocationResult) {
                super.onLocationResult(locationResult)
                Log.e("Ubica",locationResult.lastLocation!!.latitude.toString() + "," + locationResult.lastLocation!!.longitude)
            }
        }
        btnMapsMap = findViewById(R.id.btnMapsMap)
        btnMapsTerrain = findViewById(R.id.btnMapsTerrain)
        btnMapsHybrid = findViewById(R.id.btnMapsHybrid)
        btnMapsPolylines = findViewById(R.id.btnMapsPolylines)

        btnMapsMap.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        btnMapsTerrain.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
        btnMapsHybrid.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        }
        btnMapsPolylines.setOnClickListener {
            showPolylines()
        }
    }
    fun showPolylines(){
        if(mMap != null){
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(20.681816488380573, -103.46275281764251),12f
                )//Estadio akron
            )
            mMap.addPolyline(
                PolylineOptions().geodesic(true)
                    .add(LatLng(20.6484149, -103.4419599))// Ubicacion de mi casa
                    /*.add(LatLng(20.705235542158878, -103.32824127827607))//Estadio Jalisco
                    .add(LatLng(20.630345275110518, -103.25157427799802))//Ceti tonala
                    .add(LatLng(20.744287796172298, -103.3111361402415))//CUAAD*/
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkAndRequestPermissions()

        val casa = LatLng(20.6484149, -103.4419599)
        mMap.addMarker(
            MarkerOptions().position(casa)
                .title("Mi Casa")
                .snippet("Ubicación de mi casa")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(casa, 18f))
        mMap.setOnMapClickListener(this)
    }


    override fun onMapClick(latLng: LatLng) {
        //Cuando el user da un solo click en cualquier
        //parte del mapa, se centra el mapa en dicha
        //ubicación y se añade una marca con una
        //imagen definida, en este caso la imagen
        //se llama mapsubicacion previamente agregada
        //a la carpeta res del proyecto

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13f))
        mMap.addMarker(
            MarkerOptions()
                .title("Marca Personal")
                .snippet("Mi sitio marcado")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapsubicacion))
                .position(latLng)
        )
    }
    private fun checkAndRequestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                //SI NO SE HA CONCEDIDO EL PERMISO, SOLICITARLO
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_LOCATION)
                Toast.makeText(this,"Se acaba de conceder el permiso Ubicacion", Toast.LENGTH_SHORT).show()

            } else {
                //Permiso ya concedido
                Toast.makeText(this,"Ya tienes el permiso Ubicacion", Toast.LENGTH_SHORT).show()
                mMap.isMyLocationEnabled = true
            }
        }else{
            //No es necesario solicitar permisos en versiones anteriores a Andr>
            Toast.makeText(this,"No es necesario solicitar el permiso Ubicacion", Toast.LENGTH_SHORT).show()
            mMap.isMyLocationEnabled = true
        }
    }
    protected fun startLocationUpdates(){
        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                locationRequest,locationCallback,null
            )
        }catch (e: SecurityException) { }
    }
    protected fun stopLocationUpdates(){
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
    override fun onDestroy() {
        super.onDestroy()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("usuarios").child(userId).child("logged")

            // Actualizar el estado en la base de datos
            userRef.setValue(false)
                .addOnSuccessListener {
                    println("El estado de 'logged' se ha actualizado a false al cerrar la aplicación.")
                }
                .addOnFailureListener {
                    println("Error al actualizar el estado al cerrar la aplicación.")
                }
        }
    }
}