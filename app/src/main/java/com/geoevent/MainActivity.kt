package com.geoevent

import LocationHelper
import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.geoevent.data.auth.SessionManager
import com.geoevent.databinding.ActivityMainBinding
import com.geoevent.ui.auth.AuthActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var binding: ActivityMainBinding
    private var lastLocation: Location? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            navigateToAuth()
            return
        }
        val locationHelper: LocationHelper = LocationHelper(applicationContext)

        // Request permission
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )

        // Start location update
        locationHelper.startLocationUpdates { location ->
            lastLocation = location

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_geoevents,
                R.id.navigation_dashboard,
                R.id.navigation_geostamps,
                R.id.navigation_message
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun makeGeostamp(item: MenuItem) {
        val text = findViewById<TextView>(R.id.text_dashboard)
        text.setText("lat: ${lastLocation?.latitude}, long: ${lastLocation?.longitude}, a:${lastLocation?.accuracy}")
        println("Making geostamp from last location, lat: ${lastLocation?.latitude}, long: ${lastLocation?.longitude}, a:${lastLocation?.accuracy}")
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        sessionManager.clearSession()
        navigateToAuth()
    }
}