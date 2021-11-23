package com.phstudio.myconnection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.phstudio.myconnection.Fragments.DeviceFragment
import com.phstudio.myconnection.Fragments.SettingsFragment
import com.phstudio.myconnection.Fragments.WifiFragment
import com.phstudio.myconnection.Fragments.WifiScannerFragment
import com.phstudio.myconnection.R.*


class MainActivity : AppCompatActivity() {


    private val WifiFragment = WifiFragment()
    private val DeviceFragment = DeviceFragment()
    private val SettingsFragment = SettingsFragment()
    private val WifiScannerFragment = WifiScannerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        replaceFragment(WifiFragment)
        val bottom_navigation = findViewById<BottomNavigationView>(id.bottom_navigation)
        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                id.it_wifi -> replaceFragment(WifiFragment)
                id.it_wifiscanner -> replaceFragment(WifiScannerFragment)
                id.it_device -> replaceFragment(DeviceFragment)
                id.it_settings -> replaceFragment(SettingsFragment)
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transtction = supportFragmentManager.beginTransaction()
        transtction.replace(id.fragment_container, fragment)
        transtction.commit()
    }

    override fun onBackPressed() {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setIcon(mipmap.ic_launcher_round)
        dialogBuilder.setMessage(resources.getString(string.close_app))
            .setCancelable(false)
            .setPositiveButton(
                resources.getString(string.Yes)
            ) { _, _ ->
                finishAffinity()
            }
            .setNegativeButton(
                resources.getString(string.No)
            ) { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle(resources.getString(string.Exit_app))
        alert.show()
    }

    override fun onPause() {
        super.onPause()
        val bottom_navigation = findViewById<BottomNavigationView>(id.bottom_navigation)
        bottom_navigation.selectedItemId = id.it_wifi
        replaceFragment(WifiFragment)
    }
}
