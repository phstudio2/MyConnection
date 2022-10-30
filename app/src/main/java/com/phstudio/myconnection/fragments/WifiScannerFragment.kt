package com.phstudio.myconnection.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phstudio.myconnection.R

class WifiScannerFragment : Fragment() {

    private var wifiManager: WifiManager? = null
    private var results: List<ScanResult>? = null
    var arrayList: ArrayList<MyData> = ArrayList()
    private var adapter: MyAdapter? = null
    private lateinit var warning: TextView
    private lateinit var srlWifiScanner: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifiscanner, container, false)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srlWifiScanner = requireView().findViewById(R.id.srlWifiScanner)
        srlWifiScanner.setOnRefreshListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (requestLocationPermission()) {
                    WifiFragment.PERMISSION_CODE_ACCEPTED -> scanWifi()
                }
            } else {
                scanWifi()
            }
        }

        val listView = view.findViewById<ListView>(R.id.wifiList)
        warning = view.findViewById(R.id.warning)

        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        adapter = MyAdapter(requireContext(), arrayList)
        listView!!.adapter = adapter
        if (!wifiManager!!.isWifiEnabled) {
            Toast.makeText(context, getString(R.string.enable_wifi), Toast.LENGTH_LONG).show()
            wifiManager!!.isWifiEnabled = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext().checkSelfPermission(
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), 0)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (requestLocationPermission()) {
                    WifiFragment.PERMISSION_CODE_ACCEPTED -> scanWifi()
                }
            } else {
                scanWifi()
            }
        }
    }

    companion object {
        const val PERMISSION_CODE_ACCEPTED = 1
        const val PERMISSION_CODE_NOT_AVAILABLE = 0
    }

    private fun requestLocationPermission(): Int {
        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            when {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this.requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this.requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_CODE_ACCEPTED
                    )
                }
            }
        } else {
            return PERMISSION_CODE_ACCEPTED
        }
        return PERMISSION_CODE_NOT_AVAILABLE
    }

    @Suppress("DEPRECATION")
    private fun scanWifi() {
        srlWifiScanner.post { srlWifiScanner.isRefreshing = true }
        arrayList.clear()
        requireContext().registerReceiver(
            wifiReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        wifiManager!!.startScan()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION", "NewApi", "MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            results = wifiManager!!.scanResults
            if (isLocationEnabled()) {
                for ((id, scanResult) in results!!.withIndex()) {
                    val freq = scanResult.frequency
                    val wifiChannel = when {
                        freq == 2484 -> 14
                        freq < 2484 -> ((freq - 2407) / 5)
                        freq in 4910..4980 -> ((freq - 4000) / 5)
                        freq < 5925 -> ((freq - 5000) / 5)
                        freq == 5935 -> 2
                        freq <= 45000 -> ((freq - 5950) / 5)
                        freq in 58320..70200 -> ((freq - 56160) / 2160)
                        else -> 0
                    }
                    arrayList.add(
                        MyData(
                            id,
                            (scanResult.SSID).toString(),
                            freq.toString() + getString(R.string.mhz),
                            wifiChannel.toString(),
                            ((scanResult.level).toString() + getString(R.string.dbm)),
                            scanResult.BSSID,
                            scanResult.capabilities
                        )
                    )
                    adapter!!.notifyDataSetChanged()
                    warning.visibility = View.GONE
                    if (srlWifiScanner.isRefreshing) {
                        srlWifiScanner.isRefreshing = false
                    }
                }
            } else {
                warning.visibility = View.VISIBLE
                Toast.makeText(context, getString(R.string.turn_position), Toast.LENGTH_SHORT).show()
                if (srlWifiScanner.isRefreshing) {
                    srlWifiScanner.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(wifiReceiver)
        super.onDestroy()
    }
}
