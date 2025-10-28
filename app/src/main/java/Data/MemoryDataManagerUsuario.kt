package Data

import Entity.Usuario

object MemoryDataManagerUsuario: iDataManagerUsuario {
    private var usuarioList = mutableListOf<Usuario>()


    override fun add(usuario: Usuario) {
        usuarioList.add(usuario)
    }
    override fun remove(id: String) {
        usuarioList.removeIf { it.Id.trim() == id.trim() }
    }
    override fun update(usuario: Usuario) {
        remove(usuario.Id)
        add(usuario)
    }
    override fun getAll(): List<Usuario> = usuarioList

    override fun getById(id: String): Usuario? {
        try {
            val result = usuarioList.filter { it.Id.trim() == id.trim() }
            return if (result.any()) result[0] else null
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getByEmail(email: String): Usuario? {
        try {
            val result = usuarioList.filter { it.Email.trim() == email.trim() }
            return if (result.any()) result[0] else null
        } catch (e: Exception) {
            throw e
        }
    }

}