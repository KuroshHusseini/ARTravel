package com.example.artravel

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ArTakeImage : AppCompatActivity() {
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_take_image)

        arFrag = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment
        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.rend_text)
            .build()
        renderableFuture.thenAccept { viewRenderable = it }


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
}