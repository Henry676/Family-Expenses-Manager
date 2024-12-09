package com.example.administrador_gastos

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MiApp : Application(), Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        // Observador del ciclo de vida del proceso completo
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // Verifica si la actividad es AppPrincipal
            if (verificarActivity(activity)) {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user != null) {
                    val userId = user.uid
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference("usuarios").child(userId).child("logged")
                    userRef.setValue(true) // logged = true solo si es AppPrincipal
                }
            }
        }
    }

    private fun verificarActivity(activity: Activity) : Boolean{
        val activityName = activity::class.java.simpleName
        when (activityName) {
            AppPrincipal::class.java.simpleName -> {
                return true
            }
            FormularioRegistro::class.java.simpleName -> {
                return true
            }
            FotoTicket::class.java.simpleName -> {
                return true
            }
            Historial::class.java.simpleName -> {
                return true
            }
            MapsActivity::class.java.simpleName -> {
                return true
            }
            ProximoPago::class.java.simpleName -> {
                return true
            }
            RecyclerComida::class.java.simpleName -> {
                return true
            }
            RecyclerExtras::class.java.simpleName -> {
                return true
            }
            RecyclerProximos::class.java.simpleName -> {
                return true
            }
            RecyclerServicios::class.java.simpleName -> {
                return true
            }
            RecyclerTransporte::class.java.simpleName -> {
                return true
            }
            SeccionComida::class.java.simpleName -> {
                return true
            }
            SeccionExtras::class.java.simpleName -> {
                return true
            }
            SeccionProximosPagos::class.java.simpleName -> {
                return true
            }
            SeccionServicios::class.java.simpleName -> {
                return true
            }
            SeccionTransporte::class.java.simpleName -> {
                return true
            }
        }
        return false
    }


    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App pasa a background (NO cambiamos logged aquí todavía)
        }
    }


    // Observador para cuando la app se termina
    inner class AppLifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            // Se ejecuta cuando la app va al background o es cerrada
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("usuarios").child(userId).child("logged")
                userRef.setValue(false) // Actualizamos logged al cerrar la app
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
