package com.example.artravel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Favorites"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}