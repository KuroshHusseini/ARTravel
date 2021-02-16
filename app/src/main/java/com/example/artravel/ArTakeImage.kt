package com.example.artravel

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.rendering.AnnotationRenderer
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper


class ArTakeImage : AppCompatActivity() {
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null


    // Step 1. ARCore Location 3rd party
    private lateinit var locationScene: LocationScene
    private var mSession: Session? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_take_image)

        var exception: Exception? = null
        var message: String? = null
        try {
            mSession = Session( /* context= */this)
        } catch (e: UnavailableArcoreNotInstalledException) {
            message = "Please install ARCore"
            exception = e
        } catch (e: UnavailableApkTooOldException) {
            message = "Please update ARCore"
            exception = e
        } catch (e: UnavailableSdkTooOldException) {
            message = "Please update this app"
            exception = e
        } catch (e: Exception) {
            message = "This device does not support AR"
            exception = e
        }

        if (message != null) {
            Log.e("PERKELE!", "Excpetion creating session", exception)
//            showSnackbarMessage(message, true)
//            Log.e(
//                com.google.ar.core.examples.java.helloar.HelloArActivity.TAG,
//                "Exception creating session",
//                exception
//            )
            return
        }


        arFrag = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment
        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.rend_text)
            .build()
        renderableFuture.thenAccept { viewRenderable = it }

        locationScene = LocationScene(this, this, mSession)

        var config = Config(mSession)
        if (!mSession?.isSupported(config)!!)
            Log.e("PERKELE!", "This devicde does not support AR", exception)

        mSession!!.configure(config)






        // Annotation at Buckingham Palace
        // Shows toast on touch

        var buckinghamPalace: LocationMarker = LocationMarker(
            0.1419,
            51.5014,
            AnnotationRenderer("Buckingham Palace")
        )

        // Set on tap Ar Plane Listener

        arFrag.setOnTapArPlaneListener { hitResult, _, _ ->
            if (viewRenderable == null) {
                return@setOnTapArPlaneListener
            }

            // Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            // Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.setParent(arFrag.arSceneView.scene)
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            // Add viewNode as anchorNode's child
            viewNode.setParent(anchorNode)
            viewNode.renderable = viewRenderable
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()
        }
    }

    override fun onResume() {
        super.onResume()

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (ARLocationPermissionHelper.hasPermission(this)) {
            if (locationScene != null) locationScene.resume()
            if (mSession != null) {
                showLoadingMessage()
                // Note that order matters - see the note in onPause(), the reverse applies here.
                try {
                    mSession!!.resume()
                } catch (e: CameraNotAvailableException) {
                    e.printStackTrace()
                }
            }
            mSurfaceView.onResume()
            mDisplayRotationHelper.onResume()
        } else {
            ARLocationPermissionHelper.requestPermission(this)
        }
    }
}