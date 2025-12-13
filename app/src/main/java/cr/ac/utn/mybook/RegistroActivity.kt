package cr.ac.utn.mybook

import Data.ApiUsuarioDataManager
import Util.SessionManager
import Util.Util
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {
    
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegistrarse: Button
    private lateinit var btnVolver: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var apiUsuarioDataManager: ApiUsuarioDataManager
    private lateinit var sessionManager: SessionManager
    
    companion object {
        private const val TAG = "RegistroActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        apiUsuarioDataManager = ApiUsuarioDataManager(this)
        sessionManager = SessionManager(this)
        
        initializeViews()
        setListeners()
    }
    
    private fun initializeViews() {
        etNombre = findViewById(R.id.etUsuario)
        etEmail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etContrasena)
        etConfirmPassword = findViewById(R.id.etConfirmarContrasena)
        btnRegistrarse = findViewById(R.id.btnRegistrarme)
        btnVolver = findViewById(R.id.btnCancelar)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setListeners() {
        btnRegistrarse.setOnClickListener { registrarUsuario() }
        btnVolver.setOnClickListener { finish() }
    }
    
    private fun registrarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        

        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!Util.isEmailValido(email)) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!Util.isPasswordValida(password)) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnRegistrarse.isEnabled = false
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Registrando usuario: $nombre, $email")
                val usuario = apiUsuarioDataManager.registro(nombre, email, password)
                
                Log.d(TAG, "Registro exitoso. Usuario ID: ${usuario.id}")
                Toast.makeText(this@RegistroActivity, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                

                val intent = Intent(this@RegistroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error en registro", e)
                Toast.makeText(this@RegistroActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                
            } finally {
                progressBar.visibility = View.GONE
                btnRegistrarse.isEnabled = true
            }
        }
    }
}
