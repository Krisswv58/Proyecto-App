package Data

import Entity.Libro

interface iDataManagerLibro {
    fun add(libro: Libro)
    fun update(libro: Libro)
    fun remove(id: String)
    fun getAll(): List<Libro>
    fun getById(id: String): Libro?
    fun getByTitulo(titulo: String): List<Libro>
    fun getByUsuarioId(usuarioId: String): List<Libro>

}