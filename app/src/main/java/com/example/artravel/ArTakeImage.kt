package com.example.artravel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.RangeSlider
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
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


class ArTakeImage : AppCompatActivity() {

    private lateinit var arFragment: ArFragment


    private var anchorNode: AnchorNode? = null

    private var selectedNode: TransformableNode? = null
    private var selectedRenderable: ModelRenderable? = null


//    private var PYRAMID_URL: String =
//        "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/fox.gltf?raw=true"

    /*
    *
    * Pyramid URL and Model
    *
    * */

    private var PYRAMID_URL: String =
        "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/PUSHILIN_pyramid.gltf"

//    private var pyramidNode: TransformableNode? = null

    // Model
    private var pyramidRenderable: ModelRenderable? = null

    /*
    *
    * END
    *
    * */

    /*
    *
    * Duck URL and Model
    *
    * */

    private var DUCK_URL: String =
        "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/PUSHILIN_pyramid.gltf"

//    private var duckNode: TransformableNode? = null

    // Model
    private var duckRenderable: ModelRenderable? = null

    /*
    *
    * END
    *
    * */

    /*
    *
    * Pyramid URL and Model
    *
    * */

    private var FOX_URL: String =
        "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/PUSHILIN_pyramid.gltf"
//    private var foxNode: TransformableNode? = null

    // Model
    private var foxRenderable: ModelRenderable? = null

    /*
    *
    * END
    *
    * */

    /*
    *
    * Pyramid URL and Model
    *
    * */

    private var LANTERN_URL: String =
        "https://raw.githubusercontent.com/thelockymichael/gltf-Sample_models/main/2.0/PUSHILIN_pyramid.gltf"
//    private var lanternNode: TransformableNode? = null

    // Model
    private var lanternRenderable: ModelRenderable? = null

    /*
    *
    * END
    *
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_take_image)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment

        GlobalScope.launch(Dispatchers.Main) {
            setupAndDownloadAllModels()
        }

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

        modelSizeSlider.addOnChangeListener { slider, value, fromUser ->

            Log.d("From", value.toString())
            Log.d("From", "pyramid $pyramidRenderable")

            selectedNode?.setParent(null)
            selectedNode?.localScale = Vector3(value, value, value)
            selectedNode?.setParent(anchorNode)
        }

        /*
        *  Setup all the buttons
        * */

        selectPyramid_btn.setOnClickListener {
            selectedRenderable = pyramidRenderable

            Log.d("Finishus", "pyramid $selectedRenderable")
        }

        selectDuck_btn.setOnClickListener {
            selectedRenderable = duckRenderable

            Log.d("Finishus", "duck $selectedRenderable")
        }

        selectFox_btn.setOnClickListener {
            selectedRenderable = foxRenderable

            Log.d("Finishus", "fox $selectedRenderable")
        }

        selectLantern_btn.setOnClickListener {
            selectedRenderable = lanternRenderable

            Log.d("Finishus", " lantern $selectedRenderable")
        }
    }

    private suspend fun setupAndDownloadAllModels() {

        var model = GlobalScope.async(Dispatchers.Main) {

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

//                    selectedRenderable = pyramidRenderable

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
                            "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/CesiumMan/glTF/CesiumMan.gltf"
                        ),
                        RenderableSource.SourceType.GLTF2
                    )
                        .setScale(0.75f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId("CesiumMan")
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    duckRenderable = renderable

                    selectedRenderable = duckRenderable

//                    selectedRenderable = pyramidRenderable

                    Log.d("Finishus", "finished duck $duckRenderable")
                    Log.d("Finishus", "finished duck $duckRenderable")

                }
                .exceptionally {
                    Log.i("Model", "cant load")
                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
                        .show()
                    null
                }

//            ModelRenderable.builder()
//                .setSource(
//                    applicationContext,
//                    RenderableSource.builder().setSource(
//                        applicationContext,
//                        Uri.parse(FOX_URL),
//                        RenderableSource.SourceType.GLTF2
//                    )
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build()
//                ).setRegistryId(FOX_URL)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    foxRenderable = renderable
//
////                    selectedRenderable = pyramidRenderable
//
//                    Log.d("Finishus", "finished fox $foxRenderable")
//                    Log.d("Finishus", "finished fox $foxRenderable")
//
//                }
//                .exceptionally {
//                    Log.i("Model", "cant load")
//                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
//                        .show()
//                    null
//                }


//            ModelRenderable.builder()
//                .setSource(
//                    applicationContext,
//                    RenderableSource.builder().setSource(
//                        applicationContext,
//                        Uri.parse(PYRAMID_URL),
//                        RenderableSource.SourceType.GLTF2
//                    )
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build()
//                ).setRegistryId(PYRAMID_URL)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    pyramidRenderable = renderable
//                }
//                .exceptionally {
//                    Log.i("Model", "cant load")
//                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
//                        .show()
//                    null
//                }

//            selectedRenderable = pyramidRenderable

//            Log.d("Finishus", "pyramid $pyramidRenderable")
//            Log.d("Finishus", "selectedRenderable $selectedRenderable")
//        }
//
//        var jotain = model.await()
//
//        if (jotain == 1) {
//            Log.d("Finishus", "I AM FINISHED")
//            Log.d("Finishus", "pyramid $pyramidRenderable")
//            Log.d("Finishus", "selectedRenderable $selectedRenderable")
//
//        }
//        Log.d("Finishus", model.await().toString())
//            ModelRenderable.builder()
//                .setSource(
//                    applicationContext,
//                    RenderableSource.builder().setSource(
//                        applicationContext,
//                        Uri.parse(DUCK_URL),
//                        RenderableSource.SourceType.GLTF2
//                    )
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build()
//                ).setRegistryId(DUCK_URL)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    duckRenderable = renderable
//                }
//                .exceptionally {
//                    Log.i("Model", "cant load")
//                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
//                        .show()
//                    null
//                }
//
//            ModelRenderable.builder()
//                .setSource(
//                    applicationContext,
//                    RenderableSource.builder().setSource(
//                        applicationContext,
//                        Uri.parse(FOX_URL),
//                        RenderableSource.SourceType.GLTF2
//                    )
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build()
//                ).setRegistryId(FOX_URL)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    foxRenderable = renderable
//                }
//                .exceptionally {
//                    Log.i("Model", "cant load")
//                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
//                        .show()
//                    null
//                }
//
//            ModelRenderable.builder()
//                .setSource(
//                    applicationContext,
//                    RenderableSource.builder().setSource(
//                        applicationContext,
//                        Uri.parse(LANTERN_URL),
//                        RenderableSource.SourceType.GLTF2
//                    )
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build()
//                ).setRegistryId(LANTERN_URL)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    lanternRenderable = renderable
//                }
//                .exceptionally {
//                    Log.i("Model", "cant load")
//                    Toast.makeText(applicationContext, "Model can't be Loaded", Toast.LENGTH_SHORT)
//                        .show()
//                    null
//                }

        }
    }

    private var renderableAstronaut: RenderableSource.Builder? = null

    private fun setUpPlane() {
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
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
        selectedNode!!.setOnTapListener { hitTestResult, motionEvent ->

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
