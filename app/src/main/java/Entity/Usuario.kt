package Entity

import android.graphics.Bitmap
import java.util.Date

class Usuario {

    private var id: String = ""
    private var name: String = ""
    private var email: String = ""
    private var password: String = ""
    private lateinit var birthday: Date
    private lateinit var photo: Bitmap

    constructor(id: String, name: String, email: String, password: String, birthday: Date, photo: Bitmap) {
        this.Id = id
        this.Name = name
        this.Email = email
        this.Password = password
        this.Birthday = birthday
        this.Photo = photo
    }
    var Id: String
        get() = this.id
        set(value) { this.id = value }
    var Name: String
        get() = this.name
        set(value) { this.name = value }
    var Email: String
        get() = this.email
        set(value) { this.email = value }
    var Password: String
        get() = this.password
        set(value) { this.password = value }
    var Birthday: Date
        get() = this.birthday
        set(value) { this.birthday = value }
    var Photo: Bitmap
        get() = this.photo
        set(value) { this.photo = value }

}