package Data

import Entity.Usuario

interface iDataManagerUsuario {
    fun add(usuario: Usuario)
    fun update(usuario: Usuario)
    fun remove(id: String)
    fun getAll(): List<Usuario>
    fun getById(id: String): Usuario?
    fun getByEmail(email: String): Usuario?
}