package com.example.artravel

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_attractions_detail.*

class AttractionsDetailActivity : AppCompatActivity() {

    private var name: String? = null
    private var bytes: ByteArray? = null

    // Image
    private var image: Bitmap? = null

    private var description: String? = null
    private var destinationLat: String? = null
    private var destinationLng: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_detail)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupActionBarWithNavController(findNavController(R.id.fragment))

        name = intent.getStringExtra("PLACENAME")
        // Set bitmap image to ImageView

        bytes = intent.getByteArrayExtra("PLACEIMAGE")
        bytes ?: return
        image = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)

        description = intent.getStringExtra("PLACEDESC")

        // Set UI elements, which are title, image, description
        setupUI()

        var bundle = Bundle()

        bundle.putString("key", "Hello wordl!")
//        bundle.putByteArray("PLACEIMAGE", bytes)
//        bundle.putString("PLACEDESC", description)

        fragment.arguments = bundle


//        showOnMap_button.setOnClickListener {
//            destinationLat = intent.getStringExtra("PLACELAT")
//            destinationLng = intent.getStringExtra("PLACELNG")
//
//            val drawRouteIntent = Intent(this, AttractionsDrawRouteActivity::class.java)
//
//            drawRouteIntent.putExtra("destinationLatitude", destinationLat)
//            drawRouteIntent.putExtra("destinationLongitude", destinationLng)
//
//            startActivity(drawRouteIntent)
//        }
    }

    private fun setupUI() {
//        tv_detail_place_name.text = name
//
//        iv_detail_place_image.setImageBitmap(image)
//
//        tv_detail_place_desc.text = description
    }
}