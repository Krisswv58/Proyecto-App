package cr.ac.utn.mybook

import Controller.LibroController
import Entity.Libro //
import Util.Util
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID


private const val REQUEST_CODE_PICK_IMAGE = 101

class AddEditBookActivity : AppCompatActivity() {


    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etPdfPath: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var ivBookCover: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button


    private lateinit var libroController: LibroController
    private var isEditMode: Boolean = false
    private var selectedBookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)

        libroController = LibroController(this)

        selectedBookId = intent.getStringExtra("BOOK_ID")
        isEditMode = selectedBookId != null

        initializeViews()
        setListeners()
        setupUIForMode()
    }

    private fun initializeViews() {

        etTitle = findViewById(R.id.etTitulo)
        etAuthor = findViewById(R.id.etAutor)
        etPdfPath = findViewById(R.id.etRutaPdf)
        etDescription = findViewById(R.id.etSynopsis)

        ivBookCover = findViewById(R.id.ivBookCover)
        ivBack = findViewById(R.id.ivBack)

        btnSave = findViewById(R.id.btnEnviar)
        btnUpdate = findViewById(R.id.btnActualizar)
        btnDelete = findViewById(R.id.btnEliminar)
        btnCancel = findViewById(R.id.btnCancelar)
    }

    private fun setListeners() {
        ivBack.setOnClickListener { finish() }
        ivBookCover.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveBook() }
        btnUpdate.setOnClickListener { showUpdateDialog() }
        btnDelete.setOnClickListener { showDeleteDialog() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun setupUIForMode() {
        if (isEditMode) {
            btnSave.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            loadBookData(selectedBookId!!)
        } else {

            btnSave.visibility = View.VISIBLE
            btnUpdate.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }
    }

    private fun loadBookData(bookId: String) {
        try {
            val libro = libroController.getLibroById(bookId)
            etTitle.setText(libro.Titulo)
            etAuthor.setText(libro.Autor)
            etDescription.setText(libro.Descripcion)
            etPdfPath.setText(libro.RutaPdf)
            ivBookCover.setImageBitmap(libro.Photo)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("AddEditBookActivity", "Error loading book: ${e.message}")
            finish()
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                ivBookCover.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("AddEditBookActivity", "Error loading image: ${e.message}")
                Toast.makeText(this, "Error al cargar la imagen. Intente de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBookPhotoBitmap(): Bitmap? {
        val drawable = ivBookCover.drawable
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            null
        }
    }



    private fun saveBook() {
        val title = etTitle.text.toString()
        val author = etAuthor.text.toString()
        val description = etDescription.text.toString()
        val pdfPath = etPdfPath.text.toString()
        val photo = getBookPhotoBitmap()


        if (!Util.isTituloValido(title) || photo == null) {
            Toast.makeText(this, getString(R.string.validacion_campo_requerido), Toast.LENGTH_SHORT).show()
            return
        }


        val newBook = Libro(
            id = UUID.randomUUID().toString(),
            titulo = title,
            autor = author,
            descripcion = description,
            rutaPdf = pdfPath,
            usuarioId = "DEFAULT_USER_ID", // TODO: Reemplazar con el ID del usuario logueado
            photo = photo
        )

        try {
            libroController.addLibro(newBook)
            Toast.makeText(this, getString(R.string.mensaje_libro_agregado), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun updateBook() {
        val id = selectedBookId ?: return
        val title = etTitle.text.toString()
        val author = etAuthor.text.toString()
        val description = etDescription.text.toString()
        val pdfPath = etPdfPath.text.toString()
        val photo = getBookPhotoBitmap()

        if (!Util.isTituloValido(title) || photo == null) {
            Toast.makeText(this, getString(R.string.validacion_campo_requerido), Toast.LENGTH_SHORT).show()
            return
        }

        val existingBook = try {
            libroController.getLibroById(id)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.LibroNoEncontrado), Toast.LENGTH_SHORT).show()
            return
        }

        val updatedBook = Libro(
            id = id,
            titulo = title,
            autor = author,
            descripcion = description,
            rutaPdf = pdfPath,
            usuarioId = existingBook.UsuarioId,
            photo = photo
        )

        try {
            libroController.updateLibro(updatedBook)
            Toast.makeText(this, getString(R.string.mensaje_libro_actualizado), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun deleteBook() {
        val id = selectedBookId ?: return

        try {
            libroController.removeLibro(id)
            Toast.makeText(this, getString(R.string.mensaje_libro_eliminado), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_actualizar))
            .setMessage(getString(R.string.DialogMessageUpdate))
            .setPositiveButton(getString(R.string.btn_actualizar)) { dialog, _ ->
                updateBook()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_eliminar))
            .setMessage(getString(R.string.DialogMessageDelete))
            .setPositiveButton(getString(R.string.btn_eliminar)) { dialog, _ ->
                deleteBook()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}