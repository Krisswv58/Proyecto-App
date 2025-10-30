package Controller


import Data.MemoryDataManagerLibro
import Data.iDataManagerLibro
import Entity.Libro
import android.content.Context
import cr.ac.utn.mybook.R

class LibroController {
    private var dataManager: iDataManagerLibro = MemoryDataManagerLibro
    private var context: Context
    constructor(context: Context) {
        this.context = context
    }
    fun addLibro(libro: Libro) {
        try {
            dataManager.add(libro)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgAgregarLibro))
        }
    }

    fun updateLibro(libro: Libro) {
        try {
            dataManager.update(libro)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgActualizarLibro))
        }
    }
    fun getLibros(): List<Libro> {
        try {
            return dataManager.getAll()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgObtenerLibros))
        }
    }
    fun getLibroById(id: String): Libro {
        try {
            val result = dataManager.getById(id)
            if (result == null) {
                throw Exception(context.getString(R.string.LibroNoEncontrado))
            }
            return result
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgGetById))
        }
    }

    fun getLibrosByUsuarioId(usuarioId: String): List<Libro> {
        try {
            return dataManager.getByUsuarioId(usuarioId)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgObtenerLibros))
        }
    }
    fun searchLibrosByTitulo(titulo: String): List<Libro> {
        try {
            return dataManager.getByTitulo(titulo)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgBuscarLibros))
        }
    }
    fun removeLibro(id: String) {
        try {
            dataManager.remove(id)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgEliminarLibro))
        }
    }
}