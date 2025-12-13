package Network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit



object ApiConfig {
    const val BASE_URL = "https://mybook-api-cristywilson-h2csd6hngtccbygu.eastus-01.azurewebsites.net/"
}


data class LibroApi(
    @SerializedName("id") val id: String? = null,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("rutaPdf") val rutaPdf: String,
    @SerializedName("usuarioId") val usuarioId: String,
    @SerializedName("photo") val photo: String,
    @SerializedName("esPublico") val esPublico: Boolean = true
)


data class UsuarioApi(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("birthday") val birthday: String? = null,
    @SerializedName("photo") val photo: String? = null,
    @SerializedName("rol") val rol: String = "usuario"
)


data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)


data class LoginResponse(
    @SerializedName("usuario") val usuario: UsuarioApi,
    @SerializedName("token") val token: String
)


data class UploadResponse(
    @SerializedName("enlaceImagen") val enlaceImagen: String? = null,
    @SerializedName("enlacePdf") val enlacePdf: String? = null
)


interface ApiService {
    

    
    @POST("api/usuarios/registro")
    suspend fun registro(@Body usuario: Map<String, String>): ApiResponse<UsuarioApi>
    
    @POST("api/usuarios/login")
    suspend fun login(@Body credentials: Map<String, String>): ApiResponse<UsuarioApi>
    
    @GET("api/usuarios/{id}")
    suspend fun obtenerPerfil(@Path("id") id: String): ApiResponse<UsuarioApi>
    

    
    @GET("api/libros")
    suspend fun obtenerLibros(@Query("usuarioId") usuarioId: String? = null): ApiResponse<List<LibroApi>>
    
    @GET("api/libros/{id}")
    suspend fun obtenerLibro(
        @Path("id") id: String,
        @Query("usuarioId") usuarioId: String? = null
    ): ApiResponse<LibroApi>
    
    @POST("api/libros")
    suspend fun crearLibro(@Body libro: LibroApi): ApiResponse<LibroApi>
    
    @PUT("api/libros/{id}")
    suspend fun actualizarLibro(
        @Path("id") id: String,
        @Body libro: LibroApi
    ): ApiResponse<LibroApi>
    
    @HTTP(method = "DELETE", path = "api/libros/{id}", hasBody = true)
    suspend fun eliminarLibro(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiResponse<Any>
    
    @GET("api/libros/buscar/titulo/{titulo}")
    suspend fun buscarPorTitulo(
        @Path("titulo") titulo: String,
        @Query("usuarioId") usuarioId: String? = null
    ): ApiResponse<List<LibroApi>>
    

    
    @Multipart
    @POST("api/libros/subir")
    suspend fun subirArchivos(
        @Part imagen: MultipartBody.Part,
        @Part pdf: MultipartBody.Part?,
        @Part("titulo") titulo: okhttp3.RequestBody,
        @Part("autor") autor: okhttp3.RequestBody,
        @Part("descripcion") descripcion: okhttp3.RequestBody,
        @Part("usuarioId") usuarioId: okhttp3.RequestBody
    ): ApiResponse<LibroApi>
}



object RetrofitClient {
    

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
