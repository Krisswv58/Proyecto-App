package Entity

import android.graphics.Bitmap

class Libro {
    private var id: String = ""
    private var titulo: String = ""
    private var autor: String = ""
    private var descripcion: String = ""
    private var rutaPdf: String = ""
    private var photoUrl: String = ""
    private var usuarioId: String = ""
    private lateinit var photo: Bitmap
    constructor(id: String, titulo: String, autor: String, descripcion: String, rutaPdf: String, usuarioId: String, photo: Bitmap) {
        this.Id = id
        this.Titulo = titulo
        this.Autor = autor
        this.Descripcion = descripcion
        this.RutaPdf = rutaPdf
        this.PhotoUrl = ""
        this.UsuarioId = usuarioId
        this.Photo = photo
    }

    var Id: String
        get() = this.id
        set(value) { this.id = value }
    var Titulo: String
        get() = this.titulo
        set(value) { this.titulo = value }
    var Autor: String
        get() = this.autor
        set(value) { this.autor = value }
    var Descripcion: String
        get() = this.descripcion
        set(value) { this.descripcion = value }
    var RutaPdf: String
        get() = this.rutaPdf
        set(value) { this.rutaPdf = value }
    var PhotoUrl: String
        get() = this.photoUrl
        set(value) { this.photoUrl = value }
    var UsuarioId: String
        get() = this.usuarioId
        set(value) { this.usuarioId = value }
    var Photo: Bitmap
        get() = this.photo
        set(value) { this.photo = value }
}