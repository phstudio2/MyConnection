package com.phstudio.myconnection.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.*
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phstudio.myconnection.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class WifiFragment : Fragment() {
    private var type = "none"
    var rangesDNS = ('0'..'9') + ('.') + (' ')
    var rangesGateway = ('0'..'9') + ('.') + (' ') + ('-') + ('>')

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val swipetorefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.swipetorefreshwifi)
        swipetorefresh.setOnRefreshListener {
            wifi()
        }
        wifi()

        val bt_more = view.findViewById<Button>(R.id.bt_more)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            bt_more.visibility = View.GONE
        }
        bt_more!!.setOnClickListener {
            if (internet()) {
                when (type) {
                    getString(R.string.wifi) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            checkPermission2()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.notsupport),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    getString(R.string.data) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            checkPermission()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.notsupport),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            getString(R.string.not_internet),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } else if (!internet()) {
                Toast.makeText(context, getString(R.string.not_internet), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun notconnected() {
        val wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(context, getString(R.string.enablewifi), Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permissionACCESS_COARSE_LOCATION =
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permissionACCESS_COARSE_LOCATION, 1001)
                Handler().postDelayed({
                    checkPermission()
                }, 1200)
            } else {
                if (checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    val permissionREAD_PHONE_STATE = arrayOf(Manifest.permission.READ_PHONE_STATE)
                    requestPermissions(permissionREAD_PHONE_STATE, 1002)
                    Handler().postDelayed({
                        checkPermission()
                    }, 1200)
                } else {
                    dataDialog()
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.notsupport), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permissionACCESS_COARSE_LOCATION2 =
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permissionACCESS_COARSE_LOCATION2, 1003)
                Handler().postDelayed({
                    checkPermission2()
                }, 1200)
            } else {
                if (checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    val permissionREAD_PHONE_STATE2 = arrayOf(Manifest.permission.READ_PHONE_STATE)
                    requestPermissions(permissionREAD_PHONE_STATE2, 1004)
                    Handler().postDelayed({
                        checkPermission2()
                    }, 1200)
                } else {
                    wifiDialog()
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.notsupport), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun getStrenght(): Int {
        val telephonyManager =
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        for (info in telephonyManager.allCellInfo) {
            return when (info) {
                is CellInfoGsm -> info.cellSignalStrength.dbm
                is CellInfoLte -> info.cellSignalStrength.dbm
                is CellInfoCdma -> info.cellSignalStrength.dbm
                is CellInfoWcdma -> info.cellSignalStrength.dbm
                is CellInfoNr -> info.cellSignalStrength.dbm
                is CellInfoTdscdma -> info.cellSignalStrength.dbm
                else -> -50
            }
        }
        return -50
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun getLevel(): Int {
        val telephonyManager =
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        for (info in telephonyManager.allCellInfo) {
            return when (info) {
                is CellInfoGsm -> info.cellSignalStrength.level
                is CellInfoLte -> info.cellSignalStrength.level
                is CellInfoCdma -> info.cellSignalStrength.level
                is CellInfoWcdma -> info.cellSignalStrength.level
                is CellInfoNr -> info.cellSignalStrength.level
                is CellInfoTdscdma -> info.cellSignalStrength.level
                else -> 0
            }
        }
        return 0
    }

    @SuppressLint("MissingPermission")
    private fun getFrequency(): String {
        val mTelephonyManager =
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = mTelephonyManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown network"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS (2.5G)"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE (2.75G)"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA -> "H (3G+)"
            TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "H+ (3G++)"
            TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> "4G"
            else -> "4G+ or 5G"
        }
    }

    @SuppressLint("MissingPermission")
    private fun getSpeed(): String {
        val mTelephonyManager =
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = mTelephonyManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GSM -> "~10-20 kbps"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "~50-100 kbps"
            TelephonyManager.NETWORK_TYPE_CDMA -> "~14-64 kbps"
            TelephonyManager.NETWORK_TYPE_EDGE -> "~100-120 Kbps"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "~400-1000 kbps"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "~600-1400 kbps"
            TelephonyManager.NETWORK_TYPE_GPRS -> "~40-50 Kbps"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "~2-14 Mbps"
            TelephonyManager.NETWORK_TYPE_HSPA -> "~0,7-1,7 Mbps"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "~1-23 Mbps"
            TelephonyManager.NETWORK_TYPE_UMTS -> "~0.4-7 Mbps"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "~1-2 Mbps"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "~5 Mbps"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "~10-20 Mbps"
            TelephonyManager.NETWORK_TYPE_IDEN -> "~25 kbps"
            TelephonyManager.NETWORK_TYPE_LTE -> "~30 Mbps"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "~30 Mbps"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "~30 Mbps"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "?"
            else -> "~50 Mbps"
        }
    }

    private fun dataDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_analyze_data, null)
        val customDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .show()
        val name = dialogView.findViewById<TextView>(R.id.name)
        val hidden = dialogView.findViewById<TextView>(R.id.hidden)
        val strength = dialogView.findViewById<TextView>(R.id.strength)
        val level = dialogView.findViewById<TextView>(R.id.level)
        val level2 = dialogView.findViewById<TextView>(R.id.level2)
        val speed = dialogView.findViewById<TextView>(R.id.speed)
        val frequency = dialogView.findViewById<TextView>(R.id.frequency)
        val mcc = dialogView.findViewById<TextView>(R.id.mcc)

        val sharedPreferences = requireContext().getSharedPreferences(
            (resources.getString(R.string.app_package)),
            Context.MODE_PRIVATE
        )
        var timetorefresh = sharedPreferences.getInt("RefreshTime", 0)
        if (timetorefresh == 0) {
            timetorefresh = 5000
        }


        if (internet()) {
            when (type) {
                getString(R.string.data) -> {
                    val tManager =
                        requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
                    val signal_strength = getLevel()
                    var signal = "None"
                    when (signal_strength) {
                        4 -> {
                            signal = getString(R.string.excellent)
                        }
                        3 -> {
                            signal = getString(R.string.good)
                        }
                        2 -> {
                            signal = getString(R.string.medium)
                        }
                        1 -> {
                            signal = getString(R.string.weak)
                        }
                        0 -> {
                            signal = "?"
                        }
                    }
                    name.text = tManager.networkOperatorName
                    speed.text = getSpeed()
                    frequency.text = getFrequency()
                    mcc.text = tManager.networkOperator
                    hidden.text = tManager.networkCountryIso
                    strength.text = (getStrenght().toString() + getString(R.string.dbm))
                    level.text = signal
                    level2.text = (getLevel().toString() + getString(R.string.five))
                }

                getString(R.string.wifi) -> {
                    customDialog.dismiss()
                }
                else -> {
                    customDialog.dismiss()
                }
            }
        } else if (!internet()) {
            customDialog.dismiss()
            notconnected()
        }

        val btDismiss = dialogView.findViewById<Button>(R.id.cancel_button)
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
    }

    private fun wifiDialog() {
        val dialogView2 = layoutInflater.inflate(R.layout.activity_analyze, null)
        val customDialog2 = AlertDialog.Builder(requireContext())
            .setView(dialogView2)
            .show()
        val name = dialogView2.findViewById<TextView>(R.id.name)
        val hidden = dialogView2.findViewById<TextView>(R.id.hidden)
        val strength = dialogView2.findViewById<TextView>(R.id.strength)
        val level = dialogView2.findViewById<TextView>(R.id.level)
        val level2 = dialogView2.findViewById<TextView>(R.id.level2)
        val speed = dialogView2.findViewById<TextView>(R.id.speed)
        val frequency = dialogView2.findViewById<TextView>(R.id.frequency)
        val channel = dialogView2.findViewById<TextView>(R.id.channel)

        val sharedPreferences = requireContext().getSharedPreferences(
            (resources.getString(R.string.app_package)),
            Context.MODE_PRIVATE
        )
        var timetorefresh = sharedPreferences.getInt("RefreshTime", 0)
        if (timetorefresh == 0) {
            timetorefresh = 5000
        }

        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val signal_strength =
                        (WifiManager.calculateSignalLevel(wifiInfo.rssi, 5))
                    var signal = "None"
                    when (signal_strength) {
                        5 -> {
                            signal = getString(R.string.excellent)
                        }
                        4 -> {
                            signal = getString(R.string.excellent)
                        }
                        3 -> {
                            signal = getString(R.string.good)
                        }
                        2 -> {
                            signal = getString(R.string.medium)
                        }
                        1 -> {
                            signal = getString(R.string.weak)
                        }
                        0 -> {
                            signal = "?"
                        }
                    }
                    strength.text = (wifiInfo.rssi.toString() + getString(R.string.dbm))
                    level.text = signal
                    level2.text = (
                            WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
                                .toString() + getString(R.string.five))
                    speed.text =
                        (wifiInfo.linkSpeed.toString() + getString(R.string.mbps))
                    frequency.text =
                        (wifiInfo.frequency.toString() + getString(R.string.mhz))
                    val freq = wifiInfo.frequency
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
                    channel.text = wifi_channel.toString()

                    if (isLocationEnabled()) {
                        name.text = (wifiInfo.ssid.replace("\"", ""))
                        val hiddenSSID = (wifiInfo.hiddenSSID)
                        if (hiddenSSID) {
                            hidden.text = getString(R.string.texttrue)
                        } else {
                            hidden.text = getString(R.string.textfalse)
                        }
                    } else {
                        name.text = getString(R.string.turnposition)
                        hidden.text = getString(R.string.turnposition)
                    }
                }
                getString(R.string.data) -> {
                    customDialog2.dismiss()
                }
                else -> {
                    customDialog2.dismiss()
                }
            }
        } else if (!internet()) {
            customDialog2.dismiss()
            notconnected()
        }

        val btDismiss = dialogView2.findViewById<Button>(R.id.cancel_button)
        btDismiss.setOnClickListener {
            customDialog2.dismiss()
        }
    }

    private fun PublicIP() {
        val publicip = requireView().findViewById<TextView>(R.id.publicip)
        var ip: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (internet()) {
                Thread {
                    try {
                        val url = URL("https://ipv4.icanhazip.com")
                        val uc: HttpsURLConnection = url.openConnection() as HttpsURLConnection
                        val br = BufferedReader(InputStreamReader(uc.inputStream))
                        var line: String?
                        val lin2 = StringBuilder()
                        while (br.readLine().also { line = it } != null) {
                            lin2.append(line)
                        }
                        ip = lin2.toString()
                    } catch (e: IOException) {
                        ip = e.localizedMessage
                    }
                    publicip.text = (ip)
                }.start()
            } else if (!internet()) {
                publicip.text = (getString(R.string.notconnected))
                notconnected()
            }
        } else {
            publicip.text = getString(R.string.notsupport)
        }
    }


    private fun Connectiontype() {
        val conectiontype = requireView().findViewById<TextView>(R.id.type)
        val image_type = requireView().findViewById<ImageView>(R.id.image_type)
        var conStat = getString(R.string.notconnected)
        var picconStat = (R.drawable.none)
        val cm =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            when {
                ni.typeName.equals("WIFI", ignoreCase = true) -> if (ni.isConnected) {
                    conStat = getString(R.string.wifi)
                    type = getString(R.string.wifi)
                    picconStat = (R.drawable.wifi)
                }
                ni.typeName.equals("MOBILE", ignoreCase = true) -> if (ni.isConnected) {
                    conStat = getString(R.string.data)
                    type = getString(R.string.data)
                    picconStat = (R.drawable.lte)
                } else {
                    conStat = getString(R.string.notconnected)
                    picconStat = (R.drawable.none)
                    type = getString(R.string.wifi)
                }
            }
        }
        image_type.setImageResource(picconStat)
        conectiontype.text = (conStat)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun InternalIP() {
        val internalip = requireView().findViewById<TextView>(R.id.internalip)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val ip: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
                    internalip.text = String.format(ip)
                }
                getString(R.string.data) -> {
                    internalip.text = getIPAddress()
                }
                else -> {
                    internalip.text = (getString(R.string.notconnected))
                }
            }
        } else if (!internet()) {
            notconnected()
            internalip.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    private fun getIPAddress(): String {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            return ex.printStackTrace().toString()
        }
        return ""
    }

    @SuppressLint("NewApi")
    private fun getDNS(): String {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!!.dnsServers.toString()
    }

    @SuppressLint("NewApi")
    private fun getGateway(): String {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!!.routes.toString()
    }

    private fun Gateway() {
        val gateway = requireView().findViewById<TextView>(R.id.gateway)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiMgr: WifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiMgr.dhcpInfo
                    gateway.text = String.format(intToIp(wifiInfo.gateway))
                }
                getString(R.string.data) -> {
                    val gatewaytext1 =
                        getGateway().filter { it in rangesGateway }.replaceBefore(">", "")
                    val gatewaytext2 = gatewaytext1.replaceFirst(" ", "").replaceAfter(" ", "")
                        .filter { it in rangesDNS }
                    gateway.text = gatewaytext2
                }
                else -> {
                    gateway.text = (getString(R.string.notconnected))
                }
            }
        } else if (!internet()) {
            gateway.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    private fun DHCP() {
        val dhcp = requireView().findViewById<TextView>(R.id.dhcp)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiMgr: WifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiMgr.dhcpInfo
                    dhcp.text = intToIp(wifiInfo.serverAddress)
                }
                getString(R.string.data) -> {
                    val gatewaytext1 =
                        getGateway().filter { it in rangesGateway }.replaceBefore(">", "")
                    val gatewaytext2 = gatewaytext1.replaceFirst(" ", "").replaceAfter(" ", "")
                        .filter { it in rangesDNS }
                    dhcp.text = gatewaytext2
                }
                else -> {
                    dhcp.text = (getString(R.string.notconnected))
                }
            }
        } else if (!internet()) {
            dhcp.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun DNS() {
        val dns1 = requireView().findViewById<TextView>(R.id.dns1)
        val dns2 = requireView().findViewById<TextView>(R.id.dns2)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiMgr: WifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiMgr.dhcpInfo
                    dns1.text = intToIp(wifiInfo.dns1)
                    dns2.text = intToIp(wifiInfo.dns2)
                }
                getString(R.string.data) -> {
                    val dns1text = getDNS().filter { it in rangesDNS }.replaceAfter(" ", "")
                    val dns2text = getDNS().filter { it in rangesDNS }.replaceBefore(" ", "")
                    dns1.text = dns1text
                    dns2.text = dns2text
                }
                else -> {
                    dns1.text = (getString(R.string.notconnected))
                    dns2.text = (getString(R.string.notconnected))
                }
            }
        } else if (!internet()) {
            dns1.text = (getString(R.string.notconnected))
            dns2.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    private fun LeaseTime() {
        val lease = requireView().findViewById<TextView>(R.id.lease)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val wifiMgr: WifiManager =
                        requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiMgr.dhcpInfo
                    if (wifiInfo.leaseDuration <= 60) {
                        val lease_time = (wifiInfo.leaseDuration)
                        lease.text = (lease_time.toString() + " " + getString(R.string.seconds))
                    }
                    if (wifiInfo.leaseDuration in 61..3600) {
                        val lease_time = (wifiInfo.leaseDuration) / 60
                        lease.text = (lease_time.toString() + " " + getString(R.string.minutes))
                    }
                    if (wifiInfo.leaseDuration in 3601..86400) {
                        val lease_time = (wifiInfo.leaseDuration) / 3600
                        lease.text = (lease_time.toString() + " " + getString(R.string.hours))
                    }
                    if (wifiInfo.leaseDuration >= 86401) {
                        val lease_time = (wifiInfo.leaseDuration) / 86400
                        lease.text = (lease_time.toString() + " " + getString(R.string.days))
                    }
                }
                getString(R.string.data) -> {
                    lease.text = (getString(R.string.datahours))
                }
                else -> {
                    lease.text = (getString(R.string.none))
                }
            }
        } else if (!internet()) {
            lease.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    private fun VPN() {
        val vpn = requireView().findViewById<TextView>(R.id.vpn)
        if (internet()) {
            if (aboutvpn()) {
                vpn.text = (getString(R.string.on))
            } else if (!aboutvpn()) {
                vpn.text = (getString(R.string.off))
            }
        } else if (!internet()) {
            vpn.text = (getString(R.string.notconnected))
            notconnected()
        }
    }

    private fun aboutvpn(): Boolean {
        var iface = ""
        try {
            for (networkInterface in Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp) iface = networkInterface.name
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true
                }
            }
        } catch (error: IOException) {
            error.printStackTrace()
        }
        return false
    }

    private fun internet(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }

    @SuppressLint("NewApi")
    private fun wifi() {
        val swipetorefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.swipetorefreshwifi)
        Connectiontype()
        InternalIP()
        Gateway()
        DHCP()
        DNS()
        LeaseTime()
        VPN()
        PublicIP()
        if (swipetorefresh.isRefreshing) {
            swipetorefresh.isRefreshing = false
        }
    }
}