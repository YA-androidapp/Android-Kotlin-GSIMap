// Copyright (c) 2021 YA-androidapp(https://github.com/YA-androidapp) All rights reserved.
package jp.gr.java_conf.ya.gsimap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    private val GSIMAP_BASE_URL = "https://cyberjapandata.gsi.go.jp/xyz/std/"
    private val GSIMAP_NOTE = "出典: 地理院タイル https://maps.gsi.go.jp/development/ichiran.html"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var map: MapView
    private lateinit var mapController: MapController
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            if (::map.isInitialized && ::mapController.isInitialized && ::myLocationOverlay.isInitialized) {
                mapController.animateTo(myLocationOverlay.myLocation)
                myLocationOverlay.enableFollowLocation()
            }
        }

        checkPermissions()
    }

    override fun onPause() {
        if (::map.isInitialized)
            map.onPause()

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (::map.isInitialized)
            map.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,  grantResults: IntArray) {
        when(requestCode) {
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initialize()
                } else {
                    finish()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun checkPermissions() {
        if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setMessage(R.string.permissions_are_requested)
                        .setPositiveButton("OK", ({_, _ ->
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
                        })).show()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            initialize()
        }
    }
    private fun initialize(){
        map = findViewById<MapView>(R.id.map)
        val tileSource = XYTileSource(
                "GSI",
                5,
                18,
                256,
                ".png",
                arrayOf(GSIMAP_BASE_URL),
                GSIMAP_NOTE)
        map.setTileSource(tileSource)
        map.setMultiTouchControls(true)

        // Controller
        mapController = map.controller as MapController
        mapController.setZoom(16.0)
        val startPoint = GeoPoint(35.681236, 139.767125);
        mapController.setCenter(startPoint);

        // ScaleBar
        val scaleBar = ScaleBarOverlay(map)
        scaleBar.setAlignRight(true)
        scaleBar.setScaleBarOffset(100, 100)
        scaleBar.setTextSize(14 * this.resources.displayMetrics.density)
        map.overlays.add(scaleBar)

        // Compass
        val compassOverlay = CompassOverlay(applicationContext,
                InternalCompassOrientationProvider(applicationContext), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        // MyLocation
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)

        // Copyright
        val copyrightOverlay = CopyrightOverlay(applicationContext)
        copyrightOverlay.setAlignRight(true)
        copyrightOverlay.setTextSize(14)
        map.overlays.add(copyrightOverlay)
    }
}