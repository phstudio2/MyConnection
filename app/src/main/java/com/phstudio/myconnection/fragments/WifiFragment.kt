package com.phstudio.myconnection.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
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
import androidx.core.app.ActivityCompat
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
    private lateinit var srlWifi: SwipeRefreshLayout
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    private var type = "none"
    private var rangesDNS = ('0'..'9') + ('.') + (' ')
    private var rangesGateway = ('0'..'9') + ('.') + (' ') + ('-') + ('>')

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        srlWifi = requireView().findViewById(R.id.srlWifi)
        telephonyManager =
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val btMore = view.findViewById<Button>(R.id.btMore)

        srlWifi.setOnRefreshListener {
            wifi()
        }
        wifi()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            btMore.visibility = View.GONE
        }

        btMore!!.setOnClickListener {
            if (internet()) {
                when (type) {
                    getString(R.string.wifi) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            this.checkPermission()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.not_support),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    getString(R.string.data) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                when (requestLocationPermission()) {
                                    PERMISSION_CODE_ACCEPTED -> this.checkPermission()
                                }
                            } else {
                                this.checkPermission()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.not_support),
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

    @Suppress("DEPRECATION")
    private fun notConnected() {
        if (!wifiManager.isWifiEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                startActivityForResult(panelIntent, 1)
            } else {
                Toast.makeText(context, getString(R.string.enable_wifi), Toast.LENGTH_LONG).show()
                wifiManager.isWifiEnabled = true
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
                Handler().postDelayed({
                    this.checkPermission()
                }, 1200)
            } else {
                if (checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 1002)
                    Handler().postDelayed({
                        this.checkPermission()
                    }, 1200)
                } else {
                    when (type) {
                        getString(R.string.wifi) -> {
                            wifiDialog()
                        }
                        getString(R.string.data) -> {
                            dataDialog()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.not_support), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("NewApi", "MissingPermission")
    private fun getStrength(): Int {
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

    @SuppressLint("NewApi", "MissingPermission")
    private fun getLevel(): Int {
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

    @Suppress("DEPRECATION", "MissingPermission")
    private fun getTechnology(): String {
        return when (telephonyManager.networkType) {
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

    @Suppress("DEPRECATION", "MissingPermission")
    private fun getSpeed(): String {
        return when (telephonyManager.networkType) {
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

        if (internet()) {
            when (type) {
                getString(R.string.data) -> {
                    var signal = getString(R.string.none)
                    when (getLevel()) {
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
                    dialogView.findViewById<TextView>(R.id.tvOperator).text =
                        telephonyManager.networkOperatorName
                    dialogView.findViewById<TextView>(R.id.tvCountry).text =
                        telephonyManager.networkCountryIso
                    dialogView.findViewById<TextView>(R.id.tvMcc).text =
                        telephonyManager.networkOperator
                    (getStrength().toString() + getString(R.string.dbm)).also {
                        dialogView.findViewById<TextView>(
                            R.id.tvStrength
                        ).text = it
                    }
                    dialogView.findViewById<TextView>(R.id.tvSignal1).text = signal
                    (getLevel().toString() + getString(R.string.five)).also {
                        dialogView.findViewById<TextView>(
                            R.id.tvSignal2
                        ).text = it
                    }
                    dialogView.findViewById<TextView>(R.id.tvSpeed).text = getSpeed()
                    dialogView.findViewById<TextView>(R.id.tvTechnology).text = getTechnology()
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
            notConnected()
        }

        dialogView.findViewById<Button>(R.id.btCancel).setOnClickListener {
            customDialog.dismiss()
        }
    }

    @Suppress("DEPRECATION")
    private fun wifiDialog() {
        val dialogView2 = layoutInflater.inflate(R.layout.activity_analyze, null)
        val customDialog2 = AlertDialog.Builder(requireContext())
            .setView(dialogView2)
            .show()
        val tvName = dialogView2.findViewById<TextView>(R.id.tvName)
        val tvHidden = dialogView2.findViewById<TextView>(R.id.tvHidden)

        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val connectionInfo = wifiManager.connectionInfo
                    var signal = "None"
                    when (WifiManager.calculateSignalLevel(connectionInfo.rssi, 5)) {
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
                    (connectionInfo.rssi.toString() + getString(R.string.dbm)).also {
                        dialogView2.findViewById<TextView>(
                            R.id.tvStrength
                        ).text = it
                    }
                    dialogView2.findViewById<TextView>(R.id.tvLevel).text = signal
                    (WifiManager.calculateSignalLevel(connectionInfo.rssi, 5)
                        .toString() + getString(R.string.five)).also {
                        dialogView2.findViewById<TextView>(
                            R.id.tvLevel2
                        ).text = it
                    }
                    (connectionInfo.linkSpeed.toString() + getString(R.string.mbps)).also {
                        dialogView2.findViewById<TextView>(
                            R.id.tvSpeed
                        ).text = it
                    }
                    (connectionInfo.frequency.toString() + getString(R.string.mhz)).also {
                        dialogView2.findViewById<TextView>(
                            R.id.tvFrequency
                        ).text = it
                    }
                    val freq = connectionInfo.frequency

                    val wifiChannel: Int = when {
                        freq == 2484 -> 14
                        freq < 2484 -> ((freq - 2407) / 5)
                        freq in 4910..4980 -> ((freq - 4000) / 5)
                        freq < 5925 -> ((freq - 5000) / 5)
                        freq == 5935 -> 2
                        freq <= 45000 -> ((freq - 5950) / 5)
                        freq in 58320..70200 -> ((freq - 56160) / 2160)
                        else -> 0
                    }
                    dialogView2.findViewById<TextView>(R.id.tvChannel).text = wifiChannel.toString()

                    if (isLocationEnabled()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            when (requestLocationPermission()) {
                                PERMISSION_CODE_ACCEPTED -> tvName.text = getSsid()
                            }
                        } else {
                            tvName.text = (connectionInfo.ssid.replace("\"", ""))
                        }
                        var hiddenSSID = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            when (requestLocationPermission()) {
                                PERMISSION_CODE_ACCEPTED -> hiddenSSID = getHidden()
                            }
                        } else {
                            hiddenSSID = (connectionInfo.hiddenSSID)
                        }
                        if (hiddenSSID) {
                            tvHidden.text = getString(R.string.text_true)
                        } else {
                            tvHidden.text = getString(R.string.text_false)
                        }
                    } else {
                        tvName.text = getString(R.string.turn_position)
                        tvHidden.text = getString(R.string.turn_position)
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
            notConnected()
        }

        dialogView2.findViewById<Button>(R.id.btCancel).setOnClickListener {
            customDialog2.dismiss()
        }
    }

    companion object {
        const val PERMISSION_CODE_ACCEPTED = 1
        const val PERMISSION_CODE_NOT_AVAILABLE = 0
    }

    private fun requestLocationPermission(): Int {
        if (checkSelfPermission(
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
    private fun getSsid(): String {
        val mWifiManager: WifiManager =
            (this.requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        val info: WifiInfo = mWifiManager.connectionInfo

        return if (info.supplicantState === SupplicantState.COMPLETED) {
            (info.ssid).replace("\"", "")
        } else {
            ""
        }
    }

    @Suppress("DEPRECATION")
    private fun getHidden(): Boolean {
        val mWifiManager: WifiManager =
            (this.requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        val info: WifiInfo = mWifiManager.connectionInfo

        return if (info.supplicantState === SupplicantState.COMPLETED) {
            (info.hiddenSSID)
        } else {
            false
        }
    }

    private fun getPublic() {
        val tvPublic = requireView().findViewById<TextView>(R.id.tvPublic)
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
                        ip = e.localizedMessage!!.toString()
                    }
                    tvPublic.text = (ip)
                }.start()
            } else if (!internet()) {
                tvPublic.text = (getString(R.string.not_connected))
                notConnected()
            }
        } else {
            tvPublic.text = getString(R.string.not_support)
        }
    }

    @Suppress("DEPRECATION")
    private fun connectionType() {
        var conStat = getString(R.string.not_connected)
        var ivStart = (R.drawable.none)
        val netInfo = connectivityManager.allNetworkInfo
        for (ni in netInfo) {
            when {
                ni.typeName.equals("WIFI", ignoreCase = true) -> if (ni.isConnected) {
                    conStat = getString(R.string.wifi)
                    type = getString(R.string.wifi)
                    ivStart = (R.drawable.ic_wifi)
                }
                ni.typeName.equals("MOBILE", ignoreCase = true) -> if (ni.isConnected) {
                    conStat = getString(R.string.data)
                    type = getString(R.string.data)
                    ivStart = (R.drawable.ic_sim)
                } else {
                    conStat = getString(R.string.not_connected)
                    ivStart = (R.drawable.none)
                    type = getString(R.string.wifi)
                }
            }
        }
        requireView().findViewById<ImageView>(R.id.ivType).setImageResource(ivStart)
        requireView().findViewById<TextView>(R.id.tvType).text = (conStat)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getInternal() {
        val tvInternal = requireView().findViewById<TextView>(R.id.tvInternal)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    val ip: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
                    tvInternal.text = String.format(ip)
                }
                getString(R.string.data) -> {
                    tvInternal.text = getIPAddress()
                }
                else -> {
                    tvInternal.text = (getString(R.string.not_connected))
                }
            }
        } else if (!internet()) {
            notConnected()
            tvInternal.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    private fun getIPAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val enumIp = en.nextElement().inetAddresses
                while (enumIp.hasMoreElements()) {
                    val inetAddress = enumIp.nextElement()
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
        return connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!!.dnsServers.toString()
    }

    @SuppressLint("NewApi")
    private fun getGateway(): String {
        return connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!!.routes.toString()
    }

    @Suppress("DEPRECATION")
    private fun gateway() {
        val tvGateway = requireView().findViewById<TextView>(R.id.tvGateway)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    tvGateway.text = String.format(intToIp(wifiManager.dhcpInfo.gateway))
                }
                getString(R.string.data) -> {
                    val gatewayText1 =
                        getGateway().filter { it in rangesGateway }.replaceBefore(">", "")
                    val gatewayText2 = gatewayText1.replaceFirst(" ", "").replaceAfter(" ", "")
                        .filter { it in rangesDNS }
                    tvGateway.text = gatewayText2
                }
                else -> {
                    tvGateway.text = (getString(R.string.not_connected))
                }
            }
        } else if (!internet()) {
            tvGateway.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    @Suppress("DEPRECATION")
    private fun getDhcp() {
        val tvDhcp = requireView().findViewById<TextView>(R.id.tvDhcp)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    tvDhcp.text = intToIp(wifiManager.dhcpInfo.serverAddress)
                }
                getString(R.string.data) -> {
                    val gatewayText1 =
                        getGateway().filter { it in rangesGateway }.replaceBefore(">", "")
                    val gatewayText2 = gatewayText1.replaceFirst(" ", "").replaceAfter(" ", "")
                        .filter { it in rangesDNS }
                    tvDhcp.text = gatewayText2
                }
                else -> {
                    tvDhcp.text = (getString(R.string.not_connected))
                }
            }
        } else if (!internet()) {
            tvDhcp.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    @Suppress("DEPRECATION")
    private fun getDns() {
        val tvDns1 = requireView().findViewById<TextView>(R.id.tvDns1)
        val tvDns2 = requireView().findViewById<TextView>(R.id.tvDns2)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    tvDns1.text = intToIp(wifiManager.dhcpInfo.dns1)
                    tvDns2.text = intToIp(wifiManager.dhcpInfo.dns2)
                }
                getString(R.string.data) -> {
                    tvDns1.text = getDNS().filter { it in rangesDNS }.replaceAfter(" ", "")
                    tvDns2.text = getDNS().filter { it in rangesDNS }.replaceBefore(" ", "")
                }
                else -> {
                    tvDns1.text = (getString(R.string.not_connected))
                    tvDns2.text = (getString(R.string.not_connected))
                }
            }
        } else if (!internet()) {
            tvDns1.text = (getString(R.string.not_connected))
            tvDns2.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    @Suppress("DEPRECATION")
    private fun getLease() {
        val tvLease = requireView().findViewById<TextView>(R.id.tvLease)
        if (internet()) {
            when (type) {
                getString(R.string.wifi) -> {
                    if (wifiManager.dhcpInfo.leaseDuration <= 60) {
                        val time = (wifiManager.dhcpInfo.leaseDuration)
                        (time.toString() + " " + getString(R.string.seconds)).also {
                            tvLease.text = it
                        }
                    }
                    if (wifiManager.dhcpInfo.leaseDuration in 61..3600) {
                        val time = (wifiManager.dhcpInfo.leaseDuration) / 60
                        (time.toString() + " " + getString(R.string.minutes)).also {
                            tvLease.text = it
                        }
                    }
                    if (wifiManager.dhcpInfo.leaseDuration in 3601..86400) {
                        val time = (wifiManager.dhcpInfo.leaseDuration) / 3600
                        (time.toString() + " " + getString(R.string.hours)).also {
                            tvLease.text = it
                        }
                    }
                    if (wifiManager.dhcpInfo.leaseDuration >= 86401) {
                        val time = (wifiManager.dhcpInfo.leaseDuration) / 86400
                        (time.toString() + " " + getString(R.string.days)).also {
                            tvLease.text = it
                        }
                    }
                }
                getString(R.string.data) -> {
                    tvLease.text = (getString(R.string.data_hours))
                }
                else -> {
                    tvLease.text = (getString(R.string.none))
                }
            }
        } else if (!internet()) {
            tvLease.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    private fun getVpn() {
        val tvVpn = requireView().findViewById<TextView>(R.id.tvVpn)
        if (internet()) {
            if (vpn()) {
                tvVpn.text = (getString(R.string.on))
            } else if (!vpn()) {
                tvVpn.text = (getString(R.string.off))
            }
        } else if (!internet()) {
            tvVpn.text = (getString(R.string.not_connected))
            notConnected()
        }
    }

    private fun vpn(): Boolean {
        var iFace = ""
        try {
            for (networkInterface in Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp) iFace = networkInterface.name
                if (iFace.contains("tun") || iFace.contains("ppp") || iFace.contains("pptp")) {
                    return true
                }
            }
        } catch (error: IOException) {
            error.printStackTrace()
        }
        return false
    }

    @Suppress("DEPRECATION")
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
        connectionType()
        getInternal()
        gateway()
        getDhcp()
        getDns()
        getLease()
        getVpn()
        getPublic()
        if (srlWifi.isRefreshing) {
            srlWifi.isRefreshing = false
        }
    }
}