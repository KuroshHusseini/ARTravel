package com.example.artravel

import android.Manifest
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
import com.google.android.material.slider.RangeSlider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


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
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/colosseum002.gltf"
        private var WALLS_CHINA: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/wallsOfChina004_lego.gltf"
        private var TAJ_MAHAL_URL: String =
            "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/tajMahal001_lego.gltf"
    }

    // Model
    private var pyramidRenderable: ModelRenderable? = null
    private var colosseumRenderable: ModelRenderable? = null
    private var wallsOfChinaRenderable: ModelRenderable? = null
    private var tajMahalRenderable: ModelRenderable? = null

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

    private var clicked = false


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
        // RangeSlider
        modelSizeSlider.addOnSliderTouchListener(object :

            RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                val values = modelSizeSlider.values
                Log.d("onStartTrackingTouch From", values[0].toString())
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                val values = modelSizeSlider.values
                Log.d("onStopTrackingTouch From", values[0].toString())
            }
        })

        modelSizeSlider.addOnChangeListener { _, value, _ ->
            Log.d("From", value.toString())
            Log.d("From", "pyramid $pyramidRenderable")

            selectedNode?.setParent(null)
            selectedNode?.localScale = Vector3(value, value, value)
            selectedNode?.setParent(anchorNode)
        }
        /*
        *  Setup all the buttons
        * */
        id_add_button.setOnClickListener {
            onAddButtonClicked()
        }

        selectPyramid_btn.setOnClickListener {
            selectedRenderable = pyramidRenderable
            Log.d("Finishus", "pyramid $selectedRenderable")
        }

        selectColosseum_btn.setOnClickListener {
            selectedRenderable = colosseumRenderable
            Log.d("Finishus", "collosseum $selectedRenderable")
        }

        selectWallsOfChina_btn.setOnClickListener {
            selectedRenderable = wallsOfChinaRenderable
            Log.d("Finishus", "walls $selectedRenderable")
        }

        selectTajMahal_btn.setOnClickListener {
            selectedRenderable = tajMahalRenderable
            Log.d("Finishus", "taj $selectedRenderable")
        }


        takePicture_btn.setOnClickListener {
            takePicture()
        }


        goBack_btn.setOnClickListener {
            finish()
        }
    }

    private fun onAddButtonClicked() {
        clicked = !clicked
        setVisiblity(clicked)
        setAnimation(clicked)
        setClickable(clicked)
    }

    private fun setVisiblity(clicked: Boolean) {
        if (!clicked) {
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

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
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

    private fun setClickable(clicked: Boolean) {
        if (clicked) {
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

    // Capture Image
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
                        .setScale(0.75f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(PYRAMID_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    pyramidRenderable = renderable

                    Log.d("Finishus", "finished $pyramidRenderable")
                    Log.d("Finishus", "finished $selectedRenderable")

                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
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
                        .setScale(0.1f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(COLOSSEUM_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    colosseumRenderable = renderable
                    selectedRenderable = colosseumRenderable
                    Log.d("Finishus", "finished duck $colosseumRenderable")
                    Log.d("Finishus", "finished duck $colosseumRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
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
                        .setScale(0.75f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(WALLS_CHINA)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    wallsOfChinaRenderable = renderable

                    Log.d("Finishus", "finished fox $wallsOfChinaRenderable")
                    Log.d("Finishus", "finished fox $wallsOfChinaRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
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
                        .setScale(0.75f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(TAJ_MAHAL_URL)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    tajMahalRenderable = renderable

                    Log.d("Finishus", "finished fox $tajMahalRenderable")
                    Log.d("Finishus", "finished fox $tajMahalRenderable")
                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
                        .show()
                    null
                }
        }
    }

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
            // Add OnTap listener to 3D model
            Toast.makeText(this, "Model was touched", Toast.LENGTH_SHORT).show()
            arFragment.arSceneView.scene.removeChild(anchorNode)
            if (anchorNode != null) {
                anchorNode.anchor?.detach()
                anchorNode.setParent(null)
            }
        }
    }
}
