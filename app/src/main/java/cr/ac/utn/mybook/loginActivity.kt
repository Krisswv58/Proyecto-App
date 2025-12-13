package cr.ac.utn.mybook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import Data.ApiUsuarioDataManager
import Util.SessionManager
import Util.Util
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class loginActivity : AppCompatActivity() {
    private lateinit var apiUsuarioDataManager: ApiUsuarioDataManager
    private lateinit var sessionManager: SessionManager
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    
    companion object {
        private const val TAG = "loginActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        apiUsuarioDataManager = ApiUsuarioDataManager(this)
        sessionManager = SessionManager(this)
        

        if (sessionManager.isLoggedIn()) {
            Util.openActivity(this, MainActivity::class.java)
            finish()
            return
        }
        
        initializeViews()
        setListeners()
    }
    
    private fun initializeViews() {
        etEmail = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnIniciarSesion)
        btnRegister = findViewById(R.id.btnRegistrarme)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            
            if (!Util.isEmailValido(email)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.isBlank()) {
                Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(email, password)
        }
        
        btnRegister.setOnClickListener { 
            Util.openActivity(this, RegistroActivity::class.java) 
        }
    }
    
    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Intentando login con email: $email")
                val usuario = apiUsuarioDataManager.login(email, password)
                
                Log.d(TAG, "Login exitoso. Usuario: ${usuario.nombre}")
                Toast.makeText(this@loginActivity, "Bienvenido ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this@loginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error en login", e)
                Toast.makeText(this@loginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                
            } finally {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }
}