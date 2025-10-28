package Util

import android.content.Context
import android.content.Intent

class Util {

    companion object {

        fun openActivity(context: Context, objClass: Class<*>) {
            val intent = Intent(context, objClass)
            context.startActivity(intent)
        }

        fun isEmailValido(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isPasswordValida(password: String): Boolean {
            return password.length >= 6
        }

        fun isTituloValido(titulo: String): Boolean {
            return titulo.isNotBlank() && titulo.length <= 100
        }
    }
}