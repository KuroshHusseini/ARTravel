package com.example.artravel.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.artravel.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref =
            activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE) ?: return
        // Get measurement system value
        val measurementValue = resources.getString(R.string.saved_measurement_system)
        val measurementSystem =
            sharedPref.getString(getString(R.string.saved_measurement_system), measurementValue)

        // Get night mode value
        val nightModeValueSetting =
            sharedPref.getBoolean(getString(R.string.saved_night_mode_setting), false)

        if (measurementSystem == "imperial") switch_metricImperial.isChecked = true

        switch_darkLight.isChecked = nightModeValueSetting

        switch_darkLight.setOnClickListener {

            var nighModeOn = false
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )
                    nighModeOn = true
                }
            }

            val sharedPref =
                activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                    ?: return@setOnClickListener

            with(sharedPref.edit()) {
                putBoolean(getString(R.string.saved_night_mode_setting), nighModeOn)
                apply()
            }
        }

        switch_metricImperial.setOnClickListener {

            var measurementSystem: String? = if (switch_metricImperial.isChecked) {
                "imperial"
            } else {
                "metric"
            }

            val sharedPref =
                activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                    ?: return@setOnClickListener

            with(sharedPref.edit()) {
                putString(getString(R.string.saved_measurement_system), measurementSystem)
                apply()
            }
        }
    }
}