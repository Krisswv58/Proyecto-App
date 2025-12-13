package Data

import Entity.Libro
import Network.ApiResponse
import Network.LibroApi
import Network.RetrofitClient
import Network.UploadResponse
import Util.ImageUtils
import Util.SessionManager
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class ApiDataManagerLibro(private val context: Context) {
    
    private val apiService = RetrofitClient.apiService
    private val sessionManager = SessionManager(context)
    
    companion object {
        private const val TAG = "ApiDataManagerLibro"
    }
    
    private fun getUserId(): String {
        return sessionManager.getCurrentUserId()
    }
    

    suspend fun addAsync(
        titulo: String,
        autor: String,
        descripcion: String,
        imagenUri: Uri,
        pdfUri: Uri?
    ): Libro = withContext(Dispatchers.IO) {
        

        val imagenFile = ImageUtils.uriToFile(context, imagenUri, "imagen_${System.currentTimeMillis()}.jpg")
        val imagenPart = ImageUtils.createImagePart(imagenFile)
        
        val pdfPart = pdfUri?.let {
            val pdfFile = ImageUtils.uriToFile(context, it, "pdf_${System.currentTimeMillis()}.pdf")
            ImageUtils.createPdfPart(pdfFile)
        }
        

        val tituloBody = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val autorBody = autor.toRequestBody("text/plain".toMediaTypeOrNull())
        val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val usuarioIdBody = getUserId().toRequestBody("text/plain".toMediaTypeOrNull())
        

        val response = apiService.subirArchivos(imagenPart, pdfPart, tituloBody, autorBody, descripcionBody, usuarioIdBody)
        

        imagenFile.delete()
        pdfUri?.let { ImageUtils.uriToFile(context, it, "temp.pdf").delete() }
        
        if (response.success && response.data != null) {
            convertirApiALibro(response.data)
        } else {
            throw Exception(response.message)
        }
    }
    

    suspend fun getAllAsync(): List<Libro> = withContext(Dispatchers.IO) {
        val response = apiService.obtenerLibros(getUserId())
        
        if (response.success && response.data != null) {
            response.data.map { convertirApiALibro(it) }
        } else {
            throw Exception(response.message)
        }
    }
    

    suspend fun getByIdAsync(id: String): Libro? = withContext(Dispatchers.IO) {
        val response = apiService.obtenerLibro(id, getUserId())
        
        if (response.success && response.data != null) {
            convertirApiALibro(response.data)
        } else {
            null
        }
    }
    

    suspend fun removeAsync(id: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "removeAsync - Intentando eliminar libro con id: $id")
        val usuarioId = getUserId()
        val body = mapOf("usuarioId" to usuarioId)
        Log.d(TAG, "removeAsync - usuarioId: $usuarioId")
        
        try {
            val response = apiService.eliminarLibro(id, body)
            Log.d(TAG, "removeAsync - Response: success=${response.success}, message=${response.message}")
            
            if (!response.success) {
                throw Exception(response.message ?: "Error al eliminar libro")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "removeAsync - HTTP Error ${e.code()}: $errorBody")
            throw Exception("Error HTTP ${e.code()}: ${errorBody ?: e.message()}")
        } catch (e: Exception) {
            Log.e(TAG, "removeAsync - Error: ${e.message}", e)
            throw e
        }
    }
    

    suspend fun updateAsync(
        id: String,
        titulo: String,
        autor: String,
        descripcion: String,
        imagenUri: Uri?,
        pdfUri: Uri?,
        enlaceImagenActual: String,
        enlacePdfActual: String
    ): Libro = withContext(Dispatchers.IO) {

        Log.d(TAG, "updateAsync - ID del libro a actualizar: $id")
        Log.d(TAG, "updateAsync - Titulo: $titulo")
        Log.d(TAG, "updateAsync - Photo URL: $enlaceImagenActual")
        

        val libroActualizado = LibroApi(
            id = id,
            titulo = titulo,
            autor = autor,
            descripcion = descripcion,
            photo = enlaceImagenActual,
            rutaPdf = enlacePdfActual,
            usuarioId = getUserId(),
            esPublico = true
        )
        
        val response = apiService.actualizarLibro(id, libroActualizado)
        
        Log.d(TAG, "updateAsync - Response success: ${response.success}")
        Log.d(TAG, "updateAsync - Response data ID: ${response.data?.id}")
        
        if (response.success && response.data != null) {
            convertirApiALibro(response.data)
        } else {
            throw Exception(response.message)
        }
    }
    

    suspend fun getByTituloAsync(titulo: String): List<Libro> = withContext(Dispatchers.IO) {
        val response = apiService.buscarPorTitulo(titulo, getUserId())
        
        if (response.success && response.data != null) {
            response.data.map { convertirApiALibro(it) }
        } else {
            emptyList()
        }
    }
    

    suspend fun getLibrosByUsuarioIdAsync(usuarioId: String): List<Libro> = withContext(Dispatchers.IO) {
        getAllAsync()
    }

    private suspend fun convertirApiALibro(libroApi: LibroApi): Libro {
        val bitmap = ImageUtils.urlToBitmap(context, libroApi.photo)
        
        return Libro(
            id = libroApi.id ?: "",
            titulo = libroApi.titulo,
            autor = libroApi.autor,
            descripcion = libroApi.descripcion,
            rutaPdf = libroApi.rutaPdf,
            usuarioId = libroApi.usuarioId,
            photo = bitmap
        ).apply {
            PhotoUrl = libroApi.photo
        }
    }
}
