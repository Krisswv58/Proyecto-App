package cr.ac.utn.mybook

import Data.ApiUsuarioDataManager
import Util.SessionManager
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var ivBack: ImageView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnActualizarPerfil: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var apiUsuarioDataManager: ApiUsuarioDataManager
    private lateinit var sessionManager: SessionManager
    
    companion object {
        private const val TAG = "ProfileActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        apiUsuarioDataManager = ApiUsuarioDataManager(this)
        sessionManager = SessionManager(this)
        
        initializeViews()
        setListeners()
        loadUserProfile()
    }
    
    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        etNombre = findViewById(R.id.etUsuario)
        etEmail = findViewById(R.id.etCorreo)
        btnActualizarPerfil = findViewById(R.id.btnRegistrarme)
        btnCerrarSesion = findViewById(R.id.btnCancelar)
        progressBar = findViewById(R.id.progressBar)
        

        btnActualizarPerfil.text = "Actualizar Perfil"
        btnCerrarSesion.text = "Cerrar Sesión"
        

        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilContrasena).visibility = android.view.View.GONE
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmarContrasena).visibility = android.view.View.GONE
    }
    
    private fun setListeners() {
        ivBack.setOnClickListener { finish() }
        btnActualizarPerfil.setOnClickListener { updateProfile() }
        btnCerrarSesion.setOnClickListener { showLogoutDialog() }
    }
    
    private fun loadUserProfile() {
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        val userId = sessionManager.getCurrentUserId()
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Cargando perfil del usuario: $userId")
                val usuario = apiUsuarioDataManager.obtenerPerfil(userId)
                
                etNombre.setText(usuario.nombre)
                etEmail.setText(usuario.email)
                Log.d(TAG, "Perfil cargado: ${usuario.nombre}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar perfil", e)
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                redirectToLogin()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateProfile() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        
        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (email.isBlank()) {
            Toast.makeText(this, "El email es requerido", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "La actualización de perfil estará disponible próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { dialog, _ ->
                sessionManager.clearSession()
                redirectToLogin()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun redirectToLogin() {
        val intent = Intent(this, loginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}