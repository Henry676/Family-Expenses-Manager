package com.example.administrador_gastos

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.io.OutputStream

class FotoTicket : AppCompatActivity() {
    private lateinit var btnTomarFoto : Button
    private lateinit var imgFoto : ImageView
    private val REQUEST_CODE_CAMERA = 1
    private val responseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult ->
            if(activityResult.resultCode == RESULT_OK){
                Toast.makeText(this,"Fotografía tomada!!!",Toast.LENGTH_SHORT).show()
                val extras = activityResult.data?.extras
                val imgBitmap = extras?.get("data") as? Bitmap//o get (deprecated)
                imgFoto.rotation = 90f
                imgFoto.setImageBitmap(imgBitmap)//Si el usuario toma la foto, se obtiene y se despliega en el ImageView (imgFoto),
                //se rota 90° antes de incrustarla.
                saveImageToStorage(imgBitmap)//Envía la imagen al metodo para que se almacene en el dispositivo movil de forma permanente
            } else {
                Toast.makeText(this,"Proceso cancelado",Toast.LENGTH_SHORT).show()
                //Si el usuario no tomó la foto, sale este mensaje
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto_ticket)

        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        imgFoto = findViewById(R.id.imgFoto)

        checkAndRequestPermissions()//Evalua si tiene o no el permiso, en caso de que no, solicita al usuario que conceda el permiso

        btnTomarFoto.setOnClickListener {
            //Instancia para abrir la camara
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//Llama la funcionalidad de la camara
            //Lo que sucede cuando la camara regresa un resultado
            responseLauncher.launch(intent)
        }
    }

    private fun checkAndRequestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
                //SI NO SE HA CONCEDIDO EL PERMISO, SOLICITARLO
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA)
                Toast.makeText(this,"Se acaba de conceder el permiso", Toast.LENGTH_SHORT).show()
            } else {
                //Permiso ya concedido
                Toast.makeText(this,"Ya tienes el permiso", Toast.LENGTH_SHORT).show()
            }
        }else{
            //No es necesario solicitar permisos en versiones anteriores a Andr>
            Toast.makeText(this,"No es necesario solicitar el permiso", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_CAMERA){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permiso concedido Camara", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Permiso denegado para usar la camara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap?){
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"photo_ ${System.currentTimeMillis()}.jpg")//Nombre del archivo photo_ + cantidad en ms que corresponde a fecha, hora actual y la extension .jpg
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream.use{
                    if(it != null){
                        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,it)
                    }
                    Toast.makeText(this,"Imagen guardada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException){
                e.printStackTrace()
                Toast.makeText(this,"Error saving image", Toast.LENGTH_SHORT).show()
            }
        }
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