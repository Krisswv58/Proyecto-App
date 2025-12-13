package cr.ac.utn.mybook

import Controller.LibroController
import Util.SessionManager
import cr.ac.utn.mybook.Adapter.LibroAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private lateinit var etSearch: TextInputEditText
    private lateinit var rvBooks: RecyclerView

    private lateinit var ivMenu: ImageView
    private lateinit var tvEmptyState: android.widget.TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var libroController: LibroController
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: LibroAdapter
    
    companion object {
        private const val TAG = "MainActivity"
    }


    private val bookActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            loadBooks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        libroController = LibroController(this)
        sessionManager = SessionManager(this)
        

        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, loginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setListeners()
        loadBooks()
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
        rvBooks = findViewById(R.id.rvBooks)
        ivMenu = findViewById(R.id.ivMenu)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)
        

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.hide()
    }

    private fun setupRecyclerView() {

        adapter = LibroAdapter(emptyList()) { selectedLibro ->
            val intent = Intent(this, BookViewerActivity::class.java).apply {
                putExtra("BOOK_ID", selectedLibro.Id)
            }
            startActivity(intent)
        }

        rvBooks.layoutManager = GridLayoutManager(this, 2)
        rvBooks.adapter = adapter
    }

    private fun setListeners() {

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBooks()
                true
            } else {
                false
            }
        }

        ivMenu.setOnClickListener {
            showNavigationMenu(it)
        }

        findViewById<android.widget.TextView>(R.id.tvTitle).setOnClickListener {
            showNavigationMenu(it)
        }
    }



    private fun loadBooks() {
        val userId = sessionManager.getCurrentUserId()
        Log.d(TAG, "Cargando libros para usuario: $userId")
        

        progressBar.visibility = View.VISIBLE
        rvBooks.visibility = View.GONE
        tvEmptyState.visibility = View.GONE
        
        lifecycleScope.launch {
            try {

                val list = libroController.getLibrosByUsuarioIdAsync(userId)
                Log.d(TAG, "Libros obtenidos: ${list.size}")
                
                adapter.updateData(list)
                

                progressBar.visibility = View.GONE
                
                if (list.isEmpty()) {
                    rvBooks.visibility = View.GONE
                    tvEmptyState.visibility = View.VISIBLE
                    
                    val userName = sessionManager.getUserName() ?: "Usuario"
                    tvEmptyState.text = """
                    ¡Hola $userName!
                    
                    Tu biblioteca está esperando por ti
                """.trimIndent()
                } else {
                    rvBooks.visibility = View.VISIBLE
                    tvEmptyState.visibility = View.GONE
                }
            } catch (e: Exception) {

                progressBar.visibility = View.GONE
                rvBooks.visibility = View.VISIBLE
                
                Log.e(TAG, "Error cargando libros: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun searchBooks() {
        val searchTerm = etSearch.text.toString().trim()

        if (searchTerm.isBlank()) {
            loadBooks()
            return
        }

        progressBar.visibility = View.VISIBLE
        rvBooks.visibility = View.GONE
        tvEmptyState.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId()
                val allUserBooks = libroController.getLibrosByUsuarioIdAsync(userId)
                val results = allUserBooks.filter { 
                    it.Titulo.contains(searchTerm, ignoreCase = true) ||
                    it.Autor.contains(searchTerm, ignoreCase = true) ||
                    it.Descripcion.contains(searchTerm, ignoreCase = true)
                }
                
                progressBar.visibility = View.GONE
                adapter.updateData(results)
                
                if (results.isEmpty()) {
                    if (allUserBooks.isEmpty()) {

                        rvBooks.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = """
                       No tienes libros aún
                        
                        ¡Agrega tu primer libro para empezar!
                    """.trimIndent()
                    } else {

                        rvBooks.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = """
                       No se encontraron libros
                        No hay resultados para '$searchTerm'
                        Intenta con otro término de búsqueda
                    """.trimIndent()
                    }
                } else {
                    rvBooks.visibility = View.VISIBLE
                    tvEmptyState.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Se encontraron ${results.size} libro(s) ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error buscando libros: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun openEditMode(bookId: String?) {
        val intent = Intent(this, AddEditBookActivity::class.java).apply {
            bookId?.let { putExtra("BOOK_ID", it) }
        }
        bookActivityLauncher.launch(intent)
    }
    
    private fun showNavigationMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)
        

        popup.menu.add(0, 1, 0, " Inicio")
        popup.menu.add(0, 2, 0, "Agregar Libro")
        popup.menu.add(0, 3, 0, "Escanear Código")
        popup.menu.add(0, 4, 0, " Mi Perfil")
        popup.menu.add(0, 5, 0, "Mis Estadísticas")
        popup.menu.add(0, 6, 0, "Cerrar Sesión")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {

                    loadBooks()
                    true
                }
                2 -> {
                    openEditMode(null)
                    true
                }
                3 -> {
                    val intent = Intent(this, ScannerActivity::class.java)
                    startActivity(intent)
                    true
                }
                4 -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                5 -> {
                    showUserStats()
                    true
                }
                6 -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    private fun showUserStats() {
        lifecycleScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId()
                val userBooks = libroController.getLibrosByUsuarioIdAsync(userId)
                val userName = sessionManager.getUserName() ?: "Usuario"
                
                val statsMessage = """
                 Estadísticas de $userName
                
                Total de libros: ${userBooks.size}
                Libros con PDF: ${userBooks.count { it.RutaPdf.isNotBlank() }}
                Libros con descripción: ${userBooks.count { it.Descripcion.isNotBlank() }}
                
                ¡Sigue leyendo!
            """.trimIndent()
                
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle(" Mis Estadísticas")
                    .setMessage(statsMessage)
                    .setPositiveButton("OK", null)
                    .show()
                    
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar estadísticas: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error al cargar estadísticas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(this, loginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 100, 0, "⋮ Menú")
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            100 -> {
                showNavigationMenu(findViewById(android.R.id.content))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}