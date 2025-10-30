package Data

import Entity.Libro

object MemoryDataManagerLibro: iDataManagerLibro{

    private var libroList = mutableListOf<Libro>()


    override fun add(libro: Libro) {
        libroList.add(libro)
    }

    override fun remove(id: String) {
        libroList.removeIf { it.Id.trim() == id.trim() }
    }

    override fun update(libro: Libro) {
        remove(libro.Id)
        add(libro)
    }

    override fun getAll(): List<Libro> = libroList

    override fun getById(id: String): Libro? {
        try {
            val result = libroList.filter { it.Id.trim() == id.trim() }
            return if (result.any()) result[0] else null
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getByTitulo(titulo: String): List<Libro> {
        try {
            return libroList.filter { it.Titulo.contains(titulo, ignoreCase = true) }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getByUsuarioId(usuarioId: String): List<Libro> {
        try {
            return libroList.filter { it.UsuarioId.trim() == usuarioId.trim() }
        } catch (e: Exception) {
            throw e
        }
    }
}