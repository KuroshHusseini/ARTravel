package com.example.artravel

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
    }

    /**
     *Finding the Navigation Controller
     *Setting Navigation Controller with the BottomNavigationView
     *
     * @author Kurosh Husseini
     * @date 23.02.2021
     */
    private fun setupViews() {
        val navController = findNavController(R.id.fragNavHost)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapsFragment,
                R.id.attractionsFragment,
                R.id.weatherFragment,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragNavHost)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}