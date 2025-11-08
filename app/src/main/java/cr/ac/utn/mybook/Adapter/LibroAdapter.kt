package cr.ac.utn.mybook.Adapter

import Entity.Libro
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cr.ac.utn.mybook.R

class LibroAdapter(
    private var libros: List<Libro>,
    private val onClick: (Libro) -> Unit
) : RecyclerView.Adapter<LibroAdapter.LibroViewHolder>() {

    class LibroViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivBookCover: ImageView = view.findViewById(R.id.ivBookCover)

        fun bind(libro: Libro, onClick: (Libro) -> Unit) {

            ivBookCover.setImageBitmap(libro.Photo)


            itemView.setOnClickListener {
                onClick(libro)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return LibroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibroViewHolder, position: Int) {
        val libro = libros[position]
        holder.bind(libro, onClick)
    }

    override fun getItemCount(): Int = libros.size


    fun updateData(newLibros: List<Libro>) {
        libros = newLibros
        notifyDataSetChanged()
    }
}