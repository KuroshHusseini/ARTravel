package com.example.artravel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_attractions_detail.*

class AttractionsDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_detail)

        tv_detail_place_name.text = intent.getStringExtra("PLACENAME")
        iv_detail_place_image.setImageResource(intent.getStringExtra("PLACEIMAGE")!!.toInt())
        tv_detail_place_desc.text = intent.getStringExtra("PLACEDESC")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }
}