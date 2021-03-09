package com.example.artravel.fragments

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.artravel.R
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_ar_take_image.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * ArTakeImage class handles UI and functionality of the AR
 *
 * @author Michael Lock & Kurosh Husseini
 * @date 23.02.2021
 */

@Suppress("DEPRECATION")
class ArTakeImage : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private var anchorNode: AnchorNode? = null
    private var selectedNode: TransformableNode? = null
    private var selectedRenderable: ModelRenderable? = null

    private lateinit var sceneView: SceneView

    companion object {
        private var PYRAMID_URL: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/PUSHILIN_pyramid.gltf"
        private var COLOSSEUM_URL: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/colosseum02.gltf"
        private var WALLS_CHINA: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/wallsOfChina004_lego.gltf"
        private var TAJ_MAHAL_URL: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/tajMahal001_lego.gltf"
    }

    // Models & Ready to be displayed variables
    private var pyramidRenderable: ModelRenderable? = null
    private var pyramidRenderableIsReady: Boolean = false

    private var colosseumRenderable: ModelRenderable? = null
    private var colosseumRenderableIsReady: Boolean = false

    private var wallsOfChinaRenderable: ModelRenderable? = null
    private var wallsOfChinaRenderableIsReady: Boolean = false

    private var tajMahalRenderable: ModelRenderable? = null
    private var tajMahalRenderableIsReady: Boolean = false

    //Animation for floating buttons
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_take_image)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment

        sceneView = arFragment.arSceneView

        GlobalScope.launch(Dispatchers.Main) {
            setupAndDownloadAllModels()
        }

        setUpPlane()

        modelSizeSlider.addOnChangeListener { _, value, _ ->
            Log.d("From", value.toString())
            Log.d("From", "pyramid $pyramidRenderable")

            selectedNode?.setParent(null)
            selectedNode?.localScale = Vector3(value, value, value)
            selectedNode?.setParent(anchorNode)
        }

        /**
         * Modified michaels code
         * Floating buttons for choosing AR models
         * Floating buttons for going back and taking image
         *
         * @author Kurosh Husseini
         * @date 23.02.2021
         */

        id_add_button.setOnClickListener {
            onAddButtonClicked()
        }

        selectPyramid_btn.setOnClickListener {
            checkIfModelIsDownloaded(pyramidRenderable)
        }

        selectColosseum_btn.setOnClickListener {
            checkIfModelIsDownloaded(colosseumRenderable)
        }

        selectWallsOfChina_btn.setOnClickListener {
            checkIfModelIsDownloaded(wallsOfChinaRenderable)
        }

        selectTajMahal_btn.setOnClickListener {
            checkIfModelIsDownloaded(tajMahalRenderable)
        }

        takePicture_btn.setOnClickListener {
            takePicture()
        }


        goBack_btn.setOnClickListener {
            finish()
        }
    }

    /**
     * checkIfModelIsDownloaded checks if selected model has been loaded. Displays spinner if
     * model hasn't been assigned. Sets ..isReady boolean to false for later use.
     *
     * Method is called when one of the buttons in pressed in AR MODELS +
     *
     * @param modelRenderable 3D model corresponds to button selected
     *
     * @author Michael Lock
     * @date 09.03.2021
     */

    private fun checkIfModelIsDownloaded(modelRenderable: ModelRenderable?) {
        if (modelRenderable == null) {
            showCustomProgressDialog()
            when (modelRenderable) {
                pyramidRenderable -> pyramidRenderableIsReady = true
                colosseumRenderable -> colosseumRenderableIsReady = true
                wallsOfChinaRenderable -> wallsOfChinaRenderableIsReady = true
                tajMahalRenderable -> tajMahalRenderableIsReady = true
            }
        } else {
            selectedRenderable = modelRenderable
        }
    }

    private fun onAddButtonClicked() {
        isOpen = !isOpen
        setVisiblity(isOpen)
        setAnimation(isOpen)
        setClickable(isOpen)

        Log.d("DBG", "isOpen $isOpen")
    }

    private fun setVisiblity(isOpen: Boolean) {
        if (isOpen) {
            selectPyramid_btn.visibility = View.VISIBLE
            selectColosseum_btn.visibility = View.VISIBLE
            selectWallsOfChina_btn.visibility = View.VISIBLE
            selectTajMahal_btn.visibility = View.VISIBLE
        } else {
            selectPyramid_btn.visibility = View.INVISIBLE
            selectColosseum_btn.visibility = View.INVISIBLE
            selectWallsOfChina_btn.visibility = View.INVISIBLE
            selectTajMahal_btn.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(isOpen: Boolean) {
        if (isOpen) {
            selectPyramid_btn.startAnimation(fromBottom)
            selectColosseum_btn.startAnimation(fromBottom)
            selectWallsOfChina_btn.startAnimation(fromBottom)
            selectTajMahal_btn.startAnimation(fromBottom)
            id_add_button.startAnimation(rotateOpen)
        } else {
            selectPyramid_btn.startAnimation(toBottom)
            selectColosseum_btn.startAnimation(toBottom)
            selectWallsOfChina_btn.startAnimation(toBottom)
            selectTajMahal_btn.startAnimation(toBottom)
            id_add_button.startAnimation(rotateClose)
        }
    }

    private fun setClickable(isOpen: Boolean) {
        if (!isOpen) {
            selectPyramid_btn.isClickable = false
            selectColosseum_btn.isClickable = false
            selectWallsOfChina_btn.isClickable = false
            selectTajMahal_btn.isClickable = false
        } else {
            selectPyramid_btn.isClickable = true
            selectColosseum_btn.isClickable = true
            selectWallsOfChina_btn.isClickable = true
            selectTajMahal_btn.isClickable = true
        }
    }

    /**
     * Take screenshot of ARSCene and save it to gallery.
     * Uses PixelCopy.
     *
     * Method is called when user presses takePicture_btn.
     *
     * @author Michael Lock
     * @date 23.02.2021
     */
    private fun takePicture() {
        val view: ArSceneView = arFragment.arSceneView
        // Create a bitmap the size of the scene view.
        val bitmap: Bitmap = Bitmap.createBitmap(
            sceneView?.width, sceneView?.height,
            Bitmap.Config.ARGB_8888
        )
        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        // Make the request to copy.

        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveMediaToStorage(bitmap)
                } catch (e: IOException) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    return@request
                }
                Toast.makeText(this, getString(R.string.get_string), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.failed_to_copy_pixels) + copyResult, Toast.LENGTH_LONG
                ).show()
            }

            handlerThread.quitSafely()
        }, Handler())
    }

    /**
     * Compress and save captured AR image as jpeg and store it in Gallery.
     *
     * This method always saves an image when called.
     *
     * @param bitmap bitmap image that was captured in AR
     * @author Michael Lock
     * @date 23.02.2021
     */

    private fun saveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        Log.d("DBG", "saveMediaToStorage")
        Log.d("DBG", "filename $filename")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }

                Log.d("DBG", "imageUri $imageUri")
                Log.d("DBG", "fos $fos")
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            val image = File(imagesDir, filename)

            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                54
            )
    }

    /**
     * setupModelOnCompletion sets corresponding boolean 3D model to TRUE on completion
     * and hides spinner.
     *
     * Method is called in setupAndDownloadAllModels method.
     *
     * @param renderableToBeAssigned e.g. pyramidRenderable 3D model
     * @param renderable 3D model that is downloaded
     *
     * @author Michael Lock
     * @date 09.03.2021
     */

    private fun setupModelOnCompletion(
        renderableToBeAssigned: ModelRenderable?,
        renderable: ModelRenderable?
    ) {
        runOnUiThread {
            hideProgressDialog()
        }
        when (renderableToBeAssigned) {
            pyramidRenderable -> {
                pyramidRenderable = renderable
                if (pyramidRenderableIsReady)
                    selectedRenderable = pyramidRenderable

                selectedRenderable = colosseumRenderable
            }

            colosseumRenderable -> {
                colosseumRenderable = renderable
                if (colosseumRenderableIsReady)
                    selectedRenderable = colosseumRenderable
            }

            wallsOfChinaRenderable -> {
                wallsOfChinaRenderable = renderable
                if (wallsOfChinaRenderableIsReady)
                    selectedRenderable = wallsOfChinaRenderable
            }

            tajMahalRenderable -> {
                tajMahalRenderable = renderable
                if (tajMahalRenderableIsReady)
                    selectedRenderable = tajMahalRenderable
            }
        }
    }

    /**
     * setupAndDownloadAllModels network thread for downloading and
     * assigning the 4 world wonder model 3D model renderables.
     *
     * @author Michael Lock
     * @date 23.02.2021
     */

    private fun setupAndDownloadAllModels() {

        GlobalScope.async(Dispatchers.Main) {

            ModelRenderable.builder()
                .setSource(
                    applicationContext,
                    RenderableSource.builder().setSource(
                        applicationContext,
                        Uri.parse(PYRAMID_URL),
                        RenderableSource.SourceType.GLTF2
                    )
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(PYRAMID_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->

                    setupModelOnCompletion(pyramidRenderable, renderable)
                    Log.d("DBG", "finished pyramid $pyramidRenderable")

                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(
                        applicationContext,
                        "Model can't be Loaded",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    null
                }

            ModelRenderable.builder()
                .setSource(
                    applicationContext,
                    RenderableSource.builder().setSource(
                        applicationContext,
                        Uri.parse(
                            COLOSSEUM_URL
                        ),
                        RenderableSource.SourceType.GLTF2
                    )
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(COLOSSEUM_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    runOnUiThread {
                        hideProgressDialog()
                    }
                    setupModelOnCompletion(colosseumRenderable, renderable)

                    Log.d("DBG", "finished colosseum $colosseumRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(
                        applicationContext,
                        "Model can't be Loaded",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    null
                }

            ModelRenderable.builder()
                .setSource(
                    applicationContext,
                    RenderableSource.builder().setSource(
                        applicationContext,
                        Uri.parse(WALLS_CHINA),
                        RenderableSource.SourceType.GLTF2
                    )
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(WALLS_CHINA)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    setupModelOnCompletion(wallsOfChinaRenderable, renderable)

                    Log.d("DBG", "finished china $wallsOfChinaRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(
                        applicationContext,
                        "Model can't be Loaded",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    null
                }

            ModelRenderable.builder()
                .setSource(
                    applicationContext,
                    RenderableSource.builder().setSource(
                        applicationContext,
                        Uri.parse(TAJ_MAHAL_URL),
                        RenderableSource.SourceType.GLTF2
                    )
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(TAJ_MAHAL_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    setupModelOnCompletion(tajMahalRenderable, renderable)

                    Log.d("DBG", "finished taj mahal $tajMahalRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(
                        applicationContext,
                        "Model can't be Loaded",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    null
                }
        }
    }

    /**
     * Methods for setting up AR.
     *
     * @author Michael Lock
     * @date 23.02.2021
     */

    private fun setUpPlane() {
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            val anchor = hitResult.createAnchor()
            anchorNode =
                AnchorNode(anchor)
            anchorNode?.setParent(arFragment.arSceneView.scene)
            detectPlane(anchorNode)
        }
    }

    private fun detectPlane(anchorNode: AnchorNode?) {
        if (selectedRenderable?.equals(pyramidRenderable) == true) {
            Log.d("Finishus", "This is a pyramid")
        }
        // Create the Transformable model
        selectedNode = TransformableNode(arFragment.transformationSystem)
        selectedNode!!.setParent(anchorNode)
        selectedNode!!.renderable = selectedRenderable
        selectedNode!!.setOnTapListener { _, _ ->
            // Add OnTap listener to delete 3D model
            Toast.makeText(this, "Model was deleted", Toast.LENGTH_SHORT).show()
            arFragment.arSceneView.scene.removeChild(anchorNode)
            if (anchorNode != null) {
                anchorNode.anchor?.detach()
                anchorNode.setParent(null)
            }
        }
    }

    private var mProgressDialog: Dialog? = null

    // Custom dialog
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }
}
