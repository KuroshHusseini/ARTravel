package com.example.artravel

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_attractions_detail.*

class AttractionsDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        tv_detail_place_name.text = intent.getStringExtra("PLACENAME")
        // Set bitmap image to ImageView
        var image = intent.getParcelableExtra<Bitmap>("PLACEIMAGE")

        iv_detail_place_image.setImageBitmap(image)

        tv_detail_place_desc.text = intent.getStringExtra("PLACEDESC")
    }
}