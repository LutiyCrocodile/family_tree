package app.familygem.share

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import app.familygem.BaseActivity
import app.familygem.Global
import app.familygem.ProgressView
import app.familygem.R
import app.familygem.Settings
import app.familygem.U
import app.familygem.constant.Extra
import app.familygem.main.DiagramFragment
import app.familygem.util.TreeUtil
import graph.gedcom.Graph
import graph.gedcom.Util
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.folg.gedcom.model.Gedcom
import java.io.File
import java.io.FileOutputStream

/**
 * Allows to share a tree by exporting it as PNG image.
 */
class SharingActivity : BaseActivity() {

    private var gedcom: Gedcom? = null
    private lateinit var tree: Settings.Tree
    private lateinit var progressView: ProgressView
    private var pngFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sharing_activity)
        val treeId = intent.getIntExtra(Extra.TREE_ID, 1)
        tree = Global.settings.getTree(treeId)
        progressView = findViewById(R.id.share_progress)
        
        lifecycleScope.launch(IO) {
            if (TreeUtil.openGedcom(treeId, false)) {
                gedcom = Global.gc
                withContext(Main) {
                    if (gedcom != null) setupInterface()
                    else finish()
                }
            } else {
                withContext(Main) { finish() }
            }
        }
    }

    private fun setupInterface() {
        // Hide all unnecessary UI elements
        findViewById<View>(R.id.share_title)?.visibility = View.GONE
        findViewById<View>(R.id.share_root)?.visibility = View.GONE
        findViewById<View>(R.id.share_submitter_title)?.visibility = View.GONE
        findViewById<View>(R.id.share_submitter)?.visibility = View.GONE
        findViewById<View>(R.id.share_allow)?.visibility = View.GONE
        
        val shareButton = findViewById<Button>(R.id.share_button)
        shareButton.text = getString(R.string.export_png)
        shareButton.setOnClickListener {
            it.isEnabled = false
            progressView.visibility = View.VISIBLE
            lifecycleScope.launch(IO) {
                generateAndSharePng()
            }
        }
    }

    /**
     * Generates PNG of the family tree diagram and shares it.
     */
    private suspend fun generateAndSharePng() {
        try {
            // Create a simple text-based tree representation as PNG
            val rootId = tree.root ?: U.findRootId(gedcom)
            val person = gedcom!!.getPerson(rootId)
            if (person == null) {
                withContext(Main) {
                    Toast.makeText(this@SharingActivity, R.string.no_useful_data, Toast.LENGTH_LONG).show()
                    restore()
                }
                return
            }
            
            // Create a simple bitmap with tree information
            val width = 1200
            val height = 1600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw white background
            canvas.drawColor(android.graphics.Color.WHITE)
            
            // Draw tree title and basic info
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 48f
                isAntiAlias = true
            }
            
            var yPos = 100f
            canvas.drawText(tree.title, 50f, yPos, paint)
            
            paint.textSize = 32f
            yPos += 80f
            canvas.drawText("${gedcom!!.people.size} ${getString(R.string.persons)}", 50f, yPos, paint)
            
            yPos += 60f
            canvas.drawText("${tree.generations} ${getString(R.string.generations)}", 50f, yPos, paint)
            
            // Draw root person info
            paint.textSize = 36f
            yPos += 100f
            canvas.drawText(U.properName(person), 50f, yPos, paint)
            
            paint.textSize = 28f
            yPos += 50f
            val dates = U.twoDates(person, false)
            if (dates.isNotEmpty()) {
                canvas.drawText(dates, 50f, yPos, paint)
            }
            
            // Save to file
            pngFile = File(cacheDir, "${tree.title}_tree.png")
            FileOutputStream(pngFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }
            bitmap.recycle()
            
            withContext(Main) {
                sharePngFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Main) {
                val errorMsg = "${e.javaClass.simpleName}: ${e.localizedMessage ?: getString(R.string.something_wrong)}"
                Toast.makeText(this@SharingActivity, errorMsg, Toast.LENGTH_LONG).show()
                restore()
            }
        }
    }

    /**
     * Shares the PNG file using Android share intent.
     */
    private fun sharePngFile() {
        if (pngFile == null || !pngFile!!.exists()) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show()
            restore()
            return
        }
        
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", pngFile!!)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, tree.title)
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_tree))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
            Toast.makeText(this, R.string.png_exported_ok, Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage ?: getString(R.string.something_wrong), Toast.LENGTH_LONG).show()
            restore()
        }
    }


    private fun restore() {
        findViewById<View>(R.id.share_button)?.isEnabled = true
        progressView.visibility = View.GONE
    }
}
