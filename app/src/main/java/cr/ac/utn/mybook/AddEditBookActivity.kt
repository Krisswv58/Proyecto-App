package cr.ac.utn.mybook

import Controller.LibroController
import Entity.Libro
import Util.SessionManager
import Util.Util
import Util.PermissionManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

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
    private lateinit var progressBar: ProgressBar

    private lateinit var libroController: LibroController
    private lateinit var sessionManager: SessionManager
    private lateinit var permissionManager: PermissionManager
    private var isEditMode: Boolean = false
    private var selectedBookId: String? = null

    private var selectedImageUri: Uri? = null
    private var selectedPdfUri: Uri? = null
    private var photoUri: Uri? = null
    private var currentPhotoUrl: String = ""
    private var currentPdfUrl: String = ""
    
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var captureImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickPdfLauncher: ActivityResultLauncher<Intent>
    
    companion object {
        private const val TAG = "AddEditBookActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)

        libroController = LibroController(this)
        sessionManager = SessionManager(this)
        permissionManager = PermissionManager(this)
        
        initializeActivityResultLaunchers()
        

        if (!permissionManager.hasAllPermissions()) {
            permissionManager.requestAllPermissions(this)
        }

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
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setListeners() {
        ivBack.setOnClickListener { finish() }
        ivBookCover.setOnClickListener { showImageSourceDialog() }
        etPdfPath.setOnClickListener { pickPdfFile() }
        etPdfPath.isFocusable = false
        etPdfPath.isClickable = true
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
        lifecycleScope.launch {
            try {
                val libro = libroController.getLibroByIdAsync(bookId)
                etTitle.setText(libro.Titulo)
                etAuthor.setText(libro.Autor)
                etDescription.setText(libro.Descripcion)
                
                currentPhotoUrl = libro.PhotoUrl
                currentPdfUrl = libro.RutaPdf ?: ""
                if (!currentPdfUrl.isBlank()) {
                    etPdfPath.setText("PDF cargado")
                }
                
                
                if (!currentPhotoUrl.isNullOrBlank()) {
                    Glide.with(this@AddEditBookActivity)
                        .load(currentPhotoUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivBookCover)
                } else if (libro.Photo != null) {
                    ivBookCover.setImageBitmap(libro.Photo)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditBookActivity, e.message, Toast.LENGTH_LONG).show()
                Log.e("AddEditBookActivity", "Error loading book: ${e.message}")
                finish()
            }
        }
    }

    private fun pickImageFromGallery() {
        if (!permissionManager.hasStoragePermission()) {
            permissionManager.requestStoragePermission(this)
            return
        }
        
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun initializeActivityResultLaunchers() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data!!.data
                try {
                    selectedImageUri = imageUri
                    val bitmap = loadBitmapFromUri(imageUri!!)
                    ivBookCover.setImageBitmap(bitmap)
                    Log.d(TAG, "Imagen seleccionada: $imageUri")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image: ${e.message}")
                    Toast.makeText(this, "Error al cargar la imagen. Intente de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {

                    val imageUri = photoUri
                    if (imageUri != null) {
                        selectedImageUri = imageUri
                        val bitmap = loadBitmapFromUri(imageUri)
                        ivBookCover.setImageBitmap(bitmap)
                        Toast.makeText(this, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Foto capturada: $imageUri")
                    } else {

                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result.data?.extras?.getParcelable("data", Bitmap::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            result.data?.extras?.getParcelable("data") as? Bitmap
                        }
                        if (bitmap != null) {
                            ivBookCover.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(this, "Error al capturar imagen.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing captured image: ${e.message}")
                    Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        pickPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val pdfUri = result.data!!.data
                if (pdfUri != null) {
                    selectedPdfUri = pdfUri
                    val pdfPath = getRealPathFromURI(pdfUri) ?: pdfUri.toString()
                    etPdfPath.setText(getFileName(pdfUri))
                    Toast.makeText(this, "PDF seleccionado: ${getFileName(pdfUri)}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "PDF seleccionado: $pdfUri")
                }
            }
        }
    }
    
    private fun pickPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            pickPdfLauncher.launch(Intent.createChooser(intent, "Seleccionar archivo PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró una aplicación para seleccionar archivos", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getRealPathFromURI(uri: Uri): String? {
        return try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri.toString()
        } catch (e: Exception) {
            uri.toString()
        }
    }
    
    private fun getFileName(uri: Uri): String {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        cursor.getString(displayNameIndex)
                    } else {
                        "archivo.pdf"
                    }
                } else {
                    "archivo.pdf"
                }
            } ?: "archivo.pdf"
        } catch (e: Exception) {
            "archivo.pdf"
        }
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw Exception("No se pudo cargar la imagen")
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
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (!Util.isTituloValido(title)) {
            Toast.makeText(this, "El título es requerido", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }


        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Guardando libro: $title, autor: $author")
                Log.d(TAG, "Imagen URI: $selectedImageUri, PDF URI: $selectedPdfUri")
                
                val libro = libroController.addLibroAsync(
                    titulo = title,
                    autor = author,
                    descripcion = description,
                    imagenUri = selectedImageUri!!,
                    pdfUri = selectedPdfUri
                )
                
                Log.d(TAG, "Libro guardado exitosamente: ${libro.Id}")
                Toast.makeText(this@AddEditBookActivity, "Libro agregado exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar libro", e)
                Toast.makeText(this@AddEditBookActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                
            } finally {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
            }
        }
    }

    private fun updateBook() {
        val id = selectedBookId ?: return
        val title = etTitle.text.toString()
        val author = etAuthor.text.toString()
        val description = etDescription.text.toString()
        val pdfPath = etPdfPath.text.toString()

    

        lifecycleScope.launch {
            try {
                val existingBook = libroController.getLibroByIdAsync(id)
                
                progressBar.visibility = View.VISIBLE
                btnUpdate.isEnabled = false
                
                val updatedBook = libroController.updateLibroAsync(
                    id = id,
                    titulo = title,
                    autor = author,
                    descripcion = description,
                    imagenUri = selectedImageUri,
                    pdfUri = selectedPdfUri,
                    enlaceImagenActual = currentPhotoUrl,
                    enlacePdfActual = currentPdfUrl
                )
                
                Toast.makeText(this@AddEditBookActivity, "Libro actualizado exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditBookActivity, e.message, Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnUpdate.isEnabled = true
            }
        }
    }

    private fun deleteBook() {
        val id = selectedBookId ?: return

        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnDelete.isEnabled = false
                
                Log.d(TAG, "deleteBook - Eliminando libro con ID: $id")
                libroController.removeLibroAsync(id)
                Log.d(TAG, "deleteBook - Libro eliminado exitosamente")
                Toast.makeText(this@AddEditBookActivity, "Libro eliminado exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "deleteBook - Error al eliminar: ${e.message}", e)
                Toast.makeText(this@AddEditBookActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnDelete.isEnabled = true
            }
        }
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle("Actualizar libro")
            .setMessage("¿Estás seguro de que quieres actualizar este libro?")
            .setPositiveButton("Actualizar") { dialog, _ ->
                updateBook()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar libro")
            .setMessage("¿Estás seguro de que quieres eliminar este libro?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteBook()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Galería", "Cámara")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar fuente de imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> captureImageFromCamera()
                }
            }
            .show()
    }

    private fun captureImageFromCamera() {
        if (!permissionManager.hasCameraPermission()) {
            permissionManager.requestCameraPermission(this)
            return
        }
        
        try {

            val photoFile = File(externalCacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(
                this,
                "cr.ac.utn.mybook.fileprovider",
                photoFile
            )
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            
            if (intent.resolveActivity(packageManager) != null) {
                captureImageLauncher.launch(intent)
            } else {
                Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Log.e("AddEditBookActivity", "Error setting up camera: ${e.message}")
            Toast.makeText(this, "Error al configurar la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PermissionManager.CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
                } else {
                    if (permissionManager.shouldShowPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                        showPermissionRationale("Cámara", "Para tomar fotos de las portadas de los libros")
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
                    }
                }
            }
            PermissionManager.STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_LONG).show()
                }
            }
            PermissionManager.ALL_PERMISSIONS_REQUEST -> {
                val deniedPermissions = mutableListOf<String>()
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i])
                    }
                }
                
                if (deniedPermissions.isEmpty()) {
                    Toast.makeText(this, "Todos los permisos concedidos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Algunos permisos fueron denegados", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showPermissionRationale(permissionName: String, explanation: String) {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("Esta aplicación necesita acceso a $permissionName $explanation. ¿Deseas conceder el permiso?")
            .setPositiveButton("Sí") { _, _ ->
                permissionManager.requestAllPermissions(this)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Sin permisos, algunas funciones no estarán disponibles", Toast.LENGTH_LONG).show()
            }
            .show()
    }
}