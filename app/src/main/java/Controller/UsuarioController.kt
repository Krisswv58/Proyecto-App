package Controller

import Data.MemoryDataManagerUsuario
import Data.iDataManagerUsuario
import Entity.Usuario
import android.content.Context
import cr.ac.utn.mybook.R

class UsuarioController {

    private var dataManager: iDataManagerUsuario = MemoryDataManagerUsuario
    private var context: Context

    constructor(context: Context) {
        this.context = context
    }
    fun addUsuario(usuario: Usuario) {
        try {
            dataManager.add(usuario)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgAdd))
        }
    }

    fun updateUsuario(usuario: Usuario) {
        try {
            dataManager.update(usuario)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgUpdate))
        }
    }
    fun getUsuarios(): List<Usuario> {
        try {
            return dataManager.getAll()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgGetAll))
        }
    }
    fun getUsuarioById(id: String): Usuario {
        try {
            val result = dataManager.getById(id)
            if (result == null) {
                throw Exception(context.getString(R.string.UsuarioNoEncontrado))
            }
            return result
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgGetById))
        }
    }
    fun login(email: String, password: String): Usuario? {
        try {
            val usuario = dataManager.getByEmail(email)
            return if (usuario != null && usuario.Password == password) usuario else null
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.ErrorMsgLogin))
        }
    }
}

