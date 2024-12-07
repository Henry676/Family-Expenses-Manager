package com.example.administrador_gastos

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmaReceiver : BroadcastReceiver() {
    val idCanal : String = "Canal01"
    override fun onReceive(context: Context, intent: Intent?) {
        val nombrePago = intent?.getStringExtra("nombrePago") ?: "Sin nombre"
        val totalPago = intent?.getStringExtra("total") ?: "0.0"
        crearNotificacion(context,nombrePago,totalPago)
    }
    private fun crearNotificacion(context: Context, nombre: String, total: String) {
        val loginIntent = Intent(context, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val loginPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // requestCode único
            loginIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent para acción del botón "Abrir aplicación"
        val botonIntent = Intent(context, Login::class.java).apply {
            putExtra("btnNotificacion", "Desde el botón de notificación")
        }
        val botonPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1, // requestCode único y diferente
            botonIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir la acción del botón
        val accion: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_notificacion,
            "Abrir aplicación",
            botonPendingIntent
        ).build()

        // Crear la notificación
        val notificacion = NotificationCompat.Builder(context, idCanal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Pago pendiente")
            .setContentText("Se acerca un pago pendiente")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "El adeudo $nombre" +
                                " está a punto de vencer. " +
                                "Por favor, pague la cantidad de $total \$MXN " +
                                "lo más pronto posible\n"
                    )
            )
            .setContentIntent(loginPendingIntent) // Abre Login al pulsar la notificación
            .setAutoCancel(true)
            .addAction(accion) // Agrega el botón a la notificación
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Mostrar la notificación
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notificacion)
    }


}
