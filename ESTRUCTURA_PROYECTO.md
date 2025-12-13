# 📚 Estructura Optimizada del Proyecto MyBook

## 🎯 Resumen de Cambios

Se han consolidado y comentado archivos para reducir la complejidad del proyecto:

### ✅ Archivos Consolidados

**Antes:**
- `Network/ApiConfig.kt`
- `Network/ApiService.kt`
- `Network/RetrofitClient.kt`
- `Network/Models/ApiModels.kt`

**Después:**
- `Network/ApiConfig.kt` (ÚNICO archivo que contiene TODO)

### 📝 Archivos Comentados (Legacy)

Estos archivos ya NO se usan pero se mantienen para compatibilidad:

- `Data/MemoryDataManagerLibro.kt` ❌ (usa `ApiDataManagerLibro` en su lugar)
- `Data/MemoryDataManagerUsuario.kt` ❌ (la API maneja usuarios)

---

## 📂 Estructura de Archivos Importantes

```
app/src/main/java/
├── Network/
│   └── ApiConfig.kt ✅ (ÚNICO archivo - contiene TODO lo de red)
│       ├── ApiConfig (URL de la API)
│       ├── Modelos de datos (LibroApi, UsuarioApi, etc.)
│       ├── ApiService (endpoints)
│       └── RetrofitClient (cliente HTTP)
│
├── Data/
│   ├── ApiDataManagerLibro.kt ✅ (USAR ESTE - conecta con API)
│   ├── MemoryDataManagerLibro.kt ❌ (legacy - comentado)
│   └── MemoryDataManagerUsuario.kt ❌ (legacy - comentado)
│
├── Controller/
│   └── LibroController.kt ✅
│       ├── Métodos legacy (síncronos) ❌ No usar
│       └── Métodos async ✅ USAR ESTOS
│
└── Util/
    ├── SessionManager.kt ✅ (gestión de sesión y token JWT)
    └── ImageUtils.kt ✅ (conversión de imágenes y archivos)
```

---

## 🔧 Qué Usar en Tu Código

### ✅ Para Cargar Libros desde la API:

```kotlin
// En tu Activity/Fragment
lifecycleScope.launch {
    try {
        val libros = libroController.getLibrosAsync()
        // Mostrar libros
    } catch (e: Exception) {
        // Manejar error
    }
}
```

### ✅ Para Agregar un Libro Nuevo:

```kotlin
lifecycleScope.launch {
    try {
        val libro = libroController.addLibroAsync(
            titulo = "Mi Libro",
            autor = "Autor",
            descripcion = "Descripción",
            imagenUri = imagenUri,  // URI de la imagen
            pdfUri = pdfUri         // URI del PDF (opcional)
        )
        // Libro creado y subido a Azure
    } catch (e: Exception) {
        // Manejar error
    }
}
```

### ❌ NO Usar (Métodos Legacy):

```kotlin
// ❌ NO USAR - Solo guarda en memoria
libroController.addLibro(libro)
libroController.getLibros()
```

---

## 🌐 Configuración de Red

Todo está en un solo archivo: `Network/ApiConfig.kt`

### Para cambiar la URL de la API:

```kotlin
object ApiConfig {
    const val BASE_URL = "https://TU-API.azurewebsites.net/"
}
```

---

## 📊 Ventajas de la Consolidación

✅ **Menos archivos** → Más fácil de navegar
✅ **Todo en un lugar** → Fácil de modificar la configuración de red
✅ **Código legacy comentado** → Claro qué NO usar
✅ **Mejor documentación** → Comentarios explican el propósito

---

## 🚀 Próximos Pasos

1. **Configurar Azure Blob Storage** en tu API
2. **Actualizar Activities** para usar métodos `*Async`
3. **Probar conexión** con la API
4. **(Opcional) Eliminar** archivos MemoryDataManager cuando todo funcione

---

## 💡 Notas Importantes

- Siempre usa `lifecycleScope.launch` para llamar métodos `suspend fun`
- Los métodos async suben archivos a Azure Blob Storage automáticamente
- Las imágenes se descargan de Azure con Glide al mostrar libros
- El token JWT se guarda automáticamente en SharedPreferences
