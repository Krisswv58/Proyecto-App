package cr.ac.utn.mybook

import Controller.LibroController
import cr.ac.utn.mybook.Adapter.LibroAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {


    private lateinit var etSearch: TextInputEditText
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivAddBook: ImageView
    private lateinit var libroController: LibroController
    private lateinit var adapter: LibroAdapter


    private val bookActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            loadBooks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        libroController = LibroController(this)

        initializeViews()
        setupRecyclerView()
        setListeners()
        loadBooks()
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
        rvBooks = findViewById(R.id.rvBooks)

        ivAddBook = findViewById(R.id.ivProfileLogo)
    }

    private fun setupRecyclerView() {

        adapter = LibroAdapter(emptyList()) { selectedLibro ->
            openEditMode(selectedLibro.Id)
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


        ivAddBook.setOnClickListener {

            openEditMode(null)
        }
    }



    private fun loadBooks() {
        try {
            val list = libroController.getLibros()
            adapter.updateData(list)
            if (list.isEmpty()) {
                Toast.makeText(this, getString(R.string.mensaje_sin_libros), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun searchBooks() {
        val searchTerm = etSearch.text.toString().trim()

        if (searchTerm.isBlank()) {
            loadBooks()
            return
        }

        try {
            val results = libroController.searchLibrosByTitulo(searchTerm)
            adapter.updateData(results)
            if (results.isEmpty()) {
                Toast.makeText(this, getString(R.string.MsgNoResultsFound), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun openEditMode(bookId: String?) {
        val intent = Intent(this, AddEditBookActivity::class.java).apply {
            bookId?.let { putExtra("BOOK_ID", it) }
        }
        bookActivityLauncher.launch(intent)
    }
}