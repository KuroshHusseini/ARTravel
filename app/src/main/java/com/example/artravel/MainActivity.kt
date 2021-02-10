package com.example.artravel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
    }

    fun setupViews() {
        // Finding the Navigation Controller
        val navController = findNavController(R.id.fragNavHost)
       val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapsFragment,
                R.id.attractionsFragment,
                R.id.weatherFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setting Navigation Controller with the BottomNavigationView
        bottomNavView.setupWithNavController(navController)
    }
}