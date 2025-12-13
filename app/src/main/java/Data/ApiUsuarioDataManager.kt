package Data

import Network.ApiResponse
import Network.RetrofitClient
import Network.UsuarioApi
import Util.SessionManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class ApiUsuarioDataManager(private val context: Context) {
    
    private val apiService = RetrofitClient.apiService
    private val sessionManager = SessionManager(context)
    
    companion object {
        private const val TAG = "ApiUsuarioDataManager"
    }

    suspend fun registro(
        nombre: String,
        email: String,
        password: String
    ): UsuarioApi = withContext(Dispatchers.IO) {
        
        val body = mapOf(
            "nombre" to nombre,
            "email" to email,
            "password" to password
        )
        
        Log.d(TAG, "Enviando registro - Body: $body")
        
        try {
            val response = apiService.registro(body)
            Log.d(TAG, "Respuesta registro - Success: ${response.success}, Message: ${response.message}")
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Usuario registrado: ${response.data.nombre}, ID: ${response.data.id}")
                sessionManager.saveSession(
                    response.data.id,
                    response.data.nombre,
                    response.data.email
                )
                response.data
            } else {
                Log.e(TAG, "Error en registro: ${response.message}")
                throw Exception(response.message ?: "Error desconocido en registro")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error HTTP ${e.code()} en registro: $errorBody")
            when (e.code()) {
                400 -> throw Exception("Datos inválidos. Verifica nombre, email y contraseña.")
                409 -> throw Exception("El email ya está registrado")
                else -> throw Exception("Error del servidor: ${e.message}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red en registro", e)
            throw Exception("Error de conexión. Verifica tu internet.")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registro", e)
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    

    suspend fun login(
        email: String,
        password: String
    ): UsuarioApi = withContext(Dispatchers.IO) {
        
        val credentials = mapOf(
            "email" to email,
            "password" to password
        )
        
        Log.d(TAG, "Enviando login - Email: $email")
        
        try {
            val response = apiService.login(credentials)
            Log.d(TAG, "Respuesta login - Success: ${response.success}, Message: ${response.message}")
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Login exitoso: ${response.data.nombre}, ID: ${response.data.id}")

                sessionManager.saveSession(
                    response.data.id,
                    response.data.nombre,
                    response.data.email
                )
                response.data
            } else {
                Log.e(TAG, "Error en login: ${response.message}")
                throw Exception(response.message ?: "Credenciales inválidas")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error HTTP ${e.code()} en login: $errorBody")
            when (e.code()) {
                400 -> throw Exception("Datos inválidos. Verifica email y contraseña.")
                401 -> throw Exception("Email o contraseña incorrectos")
                404 -> throw Exception("Usuario no encontrado")
                else -> throw Exception("Error del servidor: ${e.message}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red en login", e)
            throw Exception("Error de conexión. Verifica tu internet.")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en login", e)
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    

    suspend fun obtenerPerfil(userId: String): UsuarioApi = withContext(Dispatchers.IO) {
        val response = apiService.obtenerPerfil(userId)
        
        if (response.success && response.data != null) {
            response.data
        } else {
            throw Exception(response.message)
        }
    }
}

