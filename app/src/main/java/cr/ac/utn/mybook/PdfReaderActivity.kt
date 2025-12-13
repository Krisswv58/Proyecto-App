package cr.ac.utn.mybook

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfReaderActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivPdfPage: ImageView
    private lateinit var tvPageNumber: TextView
    private lateinit var btnPreviousPage: MaterialButton
    private lateinit var btnNextPage: MaterialButton

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var currentPageIndex = 0
    private var pdfFile: File? = null

    companion object {
        const val EXTRA_PDF_URL = "pdf_url"
        const val EXTRA_BOOK_TITLE = "book_title"
        private const val TAG = "PdfReaderActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reader)

        initializeViews()
        setupToolbar()

        val pdfUrl = intent.getStringExtra(EXTRA_PDF_URL)
        val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: "PDF"

        toolbar.title = bookTitle

        if (pdfUrl.isNullOrBlank()) {
            Toast.makeText(this, "No se proporcionó URL del PDF", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadPdf(pdfUrl)
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbarPdf)
        ivPdfPage = findViewById(R.id.ivPdfPage)
        tvPageNumber = findViewById(R.id.tvPageNumber)
        btnPreviousPage = findViewById(R.id.btnPreviousPage)
        btnNextPage = findViewById(R.id.btnNextPage)
        
        btnPreviousPage.setOnClickListener { previousPage() }
        btnNextPage.setOnClickListener { nextPage() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadPdf(pdfUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Descargando PDF desde: $pdfUrl")


                val url = URL(pdfUrl)
                val connection = url.openConnection()
                connection.connect()

                pdfFile = File(cacheDir, "temp_${System.currentTimeMillis()}.pdf")
                FileOutputStream(pdfFile).use { output ->
                    connection.getInputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                Log.d(TAG, "PDF descargado exitosamente")


                val fileDescriptor = ParcelFileDescriptor.open(
                    pdfFile,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
                pdfRenderer = PdfRenderer(fileDescriptor)

                withContext(Dispatchers.Main) {
                    setupPageControls()
                    showPage(0)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar PDF: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PdfReaderActivity,
                        "Error al cargar PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun setupPageControls() {
        updatePageInfo()
        updateButtonStates()
    }
    
    private fun previousPage() {
        if (currentPageIndex > 0) {
            showPage(currentPageIndex - 1)
        }
    }
    
    private fun nextPage() {
        val pageCount = pdfRenderer?.pageCount ?: 0
        if (currentPageIndex < pageCount - 1) {
            showPage(currentPageIndex + 1)
        }
    }

    private fun showPage(index: Int) {
        pdfRenderer?.let { renderer ->
            if (index < 0 || index >= renderer.pageCount) return

            currentPage?.close()

            currentPage = renderer.openPage(index).also { page ->
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                ivPdfPage.setImageBitmap(bitmap)
                currentPageIndex = index
                updatePageInfo()
                updateButtonStates()
            }
        }
    }

    private fun updatePageInfo() {
        val pageCount = pdfRenderer?.pageCount ?: 0
        tvPageNumber.text = "${currentPageIndex + 1} / $pageCount"
    }
    
    private fun updateButtonStates() {
        val pageCount = pdfRenderer?.pageCount ?: 0
        btnPreviousPage.isEnabled = currentPageIndex > 0
        btnNextPage.isEnabled = currentPageIndex < pageCount - 1
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
        pdfFile?.delete()
    }
}
