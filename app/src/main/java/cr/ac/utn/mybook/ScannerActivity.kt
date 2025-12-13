package cr.ac.utn.mybook

import Controller.LibroController
import Entity.Libro
import Util.SessionManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class ScannerActivity : AppCompatActivity() {
    
    private lateinit var btnManualEntry: Button
    private lateinit var libroController: LibroController
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_book)
        
        libroController = LibroController(this)
        sessionManager = SessionManager(this)
        
        initializeViews()
        setListeners()
    }
    
    private fun initializeViews() {
        btnManualEntry = findViewById(R.id.btnManualEntry)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }
    
    private fun setListeners() {
        btnManualEntry.setOnClickListener { 

            val mockISBN = "978-0123456789"
            processScanResult(mockISBN, "Manual")
        }
    }
    
    private fun processScanResult(scannedContent: String, format: String) {
        lifecycleScope.launch {
            try {
                val existingBooks = libroController.getLibrosAsync()
                val existingBook = existingBooks.find { it.Titulo.contains(scannedContent) || it.RutaPdf.contains(scannedContent) }
                
                if (existingBook != null) {
                    Toast.makeText(this@ScannerActivity, "Este libro ya existe en tu biblioteca", Toast.LENGTH_LONG).show()
                    return@launch
                }

                Toast.makeText(this@ScannerActivity, "Código escaneado: $scannedContent\nAbriendo formulario para agregar libro...", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this@ScannerActivity, AddEditBookActivity::class.java)
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@ScannerActivity, "Error al procesar el código: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}