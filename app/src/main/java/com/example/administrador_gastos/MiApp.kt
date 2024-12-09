import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MiApp : Application(), Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        // Detectar cuando el proceso completo de la app se termina
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App vuelve al foreground
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App en background - NO cambiamos logged
        }
    }

    fun logout() {
        // Cerrar sesión explícitamente
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("usuarios").child(userId).child("logged")
            userRef.setValue(false).addOnCompleteListener {
                auth.signOut()
            }
        }
    }

    // Clase para detectar la terminación del proceso
    inner class AppLifecycleObserver : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
            // App completamente detenida, no en segundo plano
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
