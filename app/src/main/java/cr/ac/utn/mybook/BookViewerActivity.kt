package cr.ac.utn.mybook

import Controller.LibroController
import Entity.Libro
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class BookViewerActivity : AppCompatActivity() {
    
    private lateinit var ivBack: ImageView
    private lateinit var ivBookCover: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvAutor: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var btnLeerPdf: Button
    private var btnEditBook: com.google.android.material.button.MaterialButton? = null
    private lateinit var progressBar: ProgressBar
    
    private lateinit var libroController: LibroController
    private var currentBook: Libro? = null
    private var isResuming = false
    
    companion object {
        private const val TAG = "BookViewerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)
        
        libroController = LibroController(this)
        
        initializeViews()
        setListeners()
        loadBookDetails()
    }
    
    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        ivBookCover = findViewById(R.id.ivBookCoverDetail)
        tvTitulo = findViewById(R.id.tvBookTitleDetail)
        tvAutor = findViewById(R.id.tvBookAuthor)
        tvDescripcion = findViewById(R.id.tvSynopsisContent)
        btnLeerPdf = findViewById(R.id.btnContinue)
        progressBar = findViewById(R.id.progressBar)
        
        btnEditBook = findViewById(R.id.btnEditBook)
        if (btnEditBook == null) {
            Log.e(TAG, "ERROR: btnEditBook es null después de findViewById")
        } else {
            Log.d(TAG, "btnEditBook inicializado correctamente")
        }
    }
    
    private fun setListeners() {
        ivBack.setOnClickListener { finish() }
        btnLeerPdf.setOnClickListener { openPdf() }
        
        btnEditBook?.setOnClickListener { 
            Log.d(TAG, "Botón editar clickeado")
            editBookInfo() 
        } ?: Log.e(TAG, "btnEditBook es null, no se puede agregar listener")
    }
    
    private fun loadBookDetails() {
        val bookId = intent.getStringExtra("BOOK_ID")
        
        if (bookId == null) {
            Toast.makeText(this, "Error: ID de libro no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        if (isResuming && currentBook != null) {
            Log.d(TAG, "Ya hay datos cargados, no se recarga")
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Cargando detalles del libro: $bookId")
                currentBook = libroController.getLibroByIdAsync(bookId)
                
                currentBook?.let { book ->
                    Log.d(TAG, "Libro cargado: ${book.Titulo}")
                    tvTitulo.text = book.Titulo
                    tvAutor.text = "Por: ${book.Autor}"
                    tvDescripcion.text = book.Descripcion.ifBlank { "Sin descripción disponible" }
                    ivBookCover.setImageBitmap(book.Photo)

                    if (book.RutaPdf.isBlank()) {
                        btnLeerPdf.text = "PDF no disponible"
                        btnLeerPdf.isEnabled = false
                    } else {
                        Log.d(TAG, "PDF disponible: ${book.RutaPdf}")
                        btnLeerPdf.text = "Leer PDF"
                        btnLeerPdf.isEnabled = true
                    }
                } ?: run {
                    Toast.makeText(this@BookViewerActivity, "Libro no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar libro", e)
                Toast.makeText(this@BookViewerActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun openPdf() {
        currentBook?.let { book ->
            if (book.RutaPdf.isBlank()) {
                Toast.makeText(this, "No hay archivo PDF asociado a este libro", Toast.LENGTH_SHORT).show()
                return
            }
            
            try {
                // Usar PdfReaderActivity interno
                val intent = Intent(this, PdfReaderActivity::class.java).apply {
                    putExtra(PdfReaderActivity.EXTRA_PDF_URL, book.RutaPdf)
                    putExtra(PdfReaderActivity.EXTRA_BOOK_TITLE, book.Titulo)
                }
                startActivity(intent)
                
            } catch (e: SecurityException) {
                Toast.makeText(this, "Error de permisos. Verifica los permisos de la app.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al abrir el PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun editBookInfo() {
        currentBook?.let { book ->
            val intent = Intent(this, AddEditBookActivity::class.java).apply {
                putExtra("BOOK_ID", book.Id)
            }
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        isResuming = true
        if (currentBook == null) {
            loadBookDetails()
        }
    }
}