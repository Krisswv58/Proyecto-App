package Controller

import Data.ApiDataManagerLibro
import Entity.Libro
import android.content.Context
import android.net.Uri
import cr.ac.utn.mybook.R


class LibroController(private val context: Context) {

    private val apiDataManager = ApiDataManagerLibro(context)


    
    suspend fun addLibroAsync(
        titulo: String,
        autor: String,
        descripcion: String,
        imagenUri: Uri,
        pdfUri: Uri?
    ): Libro {
        return try {
            apiDataManager.addAsync(titulo, autor, descripcion, imagenUri, pdfUri)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgAgregarLibro) + ": ${e.message}")
        }
    }
    
  
    suspend fun getLibrosAsync(): List<Libro> {
        return try {
            apiDataManager.getAllAsync()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgObtenerLibros) + ": ${e.message}")
        }
    }
    
    suspend fun getLibroByIdAsync(id: String): Libro {
        return try {
            apiDataManager.getByIdAsync(id) 
                ?: throw Exception(context.getString(R.string.LibroNoEncontrado))
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgGetById) + ": ${e.message}")
        }
    }
    
    suspend fun removeLibroAsync(id: String) {
        try {
            apiDataManager.removeAsync(id)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgEliminarLibro) + ": ${e.message}")
        }
    }
    
    suspend fun updateLibroAsync(
        id: String,
        titulo: String,
        autor: String,
        descripcion: String,
        imagenUri: Uri?,
        pdfUri: Uri?,
        enlaceImagenActual: String,
        enlacePdfActual: String
    ): Libro {
        return try {
            apiDataManager.updateAsync(id, titulo, autor, descripcion, imagenUri, pdfUri, enlaceImagenActual, enlacePdfActual)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgActualizarLibro) + ": ${e.message}")
        }
    }
    
    suspend fun searchLibrosByTituloAsync(titulo: String): List<Libro> {
        return try {
            apiDataManager.getByTituloAsync(titulo)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgBuscarLibros) + ": ${e.message}")
        }
    }
    
    suspend fun getLibrosByUsuarioIdAsync(usuarioId: String): List<Libro> {
        return try {
            apiDataManager.getLibrosByUsuarioIdAsync(usuarioId)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgObtenerLibros) + ": ${e.message}")
        }
    }
}