
package Util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

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


        fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
            return try {
                val contentResolver = context.contentResolver
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {

                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        fun cameraDataToBitmap(data: Intent?): Bitmap? {
            return data?.extras?.get("data") as? Bitmap
        }
    }
}