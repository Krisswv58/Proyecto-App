package Util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object ImageUtils {
    

    suspend fun urlToBitmap(context: Context, url: String): Bitmap = withContext(Dispatchers.IO) {
        try {
            if (url.isBlank()) {
                return@withContext crearBitmapPorDefecto()
            }
            
            Glide.with(context)
                .asBitmap()
                .load(url)
                .submit()
                .get()
        } catch (e: Exception) {
            crearBitmapPorDefecto()
        }
    }
    

    fun crearBitmapPorDefecto(): Bitmap {
        return Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.LTGRAY)
        }
    }
    

    fun uriToFile(context: Context, uri: Uri, fileName: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
    

    fun createImagePart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("imagen", file.name, requestFile)
    }
    

    fun createPdfPart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("pdf", file.name, requestFile)
    }
}
