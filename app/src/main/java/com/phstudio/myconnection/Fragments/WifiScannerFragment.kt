package com.phstudio.myconnection.Fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
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
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phstudio.myconnection.R

class WifiScannerFragment : Fragment() {

    private var wifiManager: WifiManager? = null
    private var results: List<ScanResult>? = null
    private val arrayList = arrayListOf<String>()
    private var adapter: ArrayAdapter<*>? = null
    private lateinit var warning: TextView
    private lateinit var swipetorefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifiscanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipetorefresh = requireView().findViewById(R.id.swipetorefreshwifiscanner)
        swipetorefresh.setOnRefreshListener {
            scanWifi()
        }
        val listView = view.findViewById<ListView>(R.id.wifiList)
        warning = view.findViewById(R.id.warning)

        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item, arrayList)
        listView!!.adapter = adapter
        if (!wifiManager!!.isWifiEnabled) {
            Toast.makeText(context, getString(R.string.enablewifi), Toast.LENGTH_LONG).show()
            wifiManager!!.isWifiEnabled = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext().checkSelfPermission(
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), 0)
        } else {
            scanWifi()
        }
    }

    private fun scanWifi() {
        swipetorefresh.post { swipetorefresh.isRefreshing = true }
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

    var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NewApi")
        override fun onReceive(context: Context, intent: Intent) {
            results = wifiManager!!.scanResults
            requireActivity().unregisterReceiver(this)
            if (isLocationEnabled()) {
                for (scanResult in results!!) {
                    val freq = scanResult.frequency
                    val wifi_channel = when {
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
                        getString(R.string.ssid) + " " + scanResult.SSID + "\n" + getString(R.string.frequency) + " " + freq.toString() + getString(
                            R.string.mhz
                        ) + " - " + getString(R.string.channel) + " " + wifi_channel.toString() + "\n" + getString(
                            R.string.strength
                        ) + " " + scanResult.level + getString(
                            R.string.dbm
                        ) + "\n" + scanResult.BSSID + "\n" + scanResult.capabilities
                    )
                    adapter!!.notifyDataSetChanged()
                    warning.visibility = View.GONE
                    if (swipetorefresh.isRefreshing) {
                        swipetorefresh.isRefreshing = false
                    }
                }
            } else {
                warning.visibility = View.VISIBLE
                Toast.makeText(context, getString(R.string.turnposition), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
