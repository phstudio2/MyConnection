package com.phstudio.myconnection.fragments

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Build.CPU_ABI
import android.os.Build.SERIAL
import android.os.Build.VERSION.RELEASE
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phstudio.myconnection.R
import java.net.NetworkInterface
import java.util.*

class DeviceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val srlDevice =
            requireView().findViewById<SwipeRefreshLayout>(R.id.srlDevice)
        srlDevice.setOnRefreshListener {
            show()
        }
        show()
    }

    @Suppress("HardwareIds", "DEPRECATION")
    private fun show() {
        val srlDevice = requireView().findViewById<SwipeRefreshLayout>(R.id.srlDevice)

        val tvVersion = requireView().findViewById<TextView>(R.id.tvVersion)
        ("$RELEASE (API: ${(Build.VERSION.SDK_INT)})").also { tvVersion.text = it }

        val tvBrand = requireView().findViewById<TextView>(R.id.tvBrand)
        tvBrand.text = (Build.BRAND).toString()

        val tvModel = requireView().findViewById<TextView>(R.id.tvModel)
        ("${Build.MODEL} (${Build.PRODUCT})").also { tvModel.text = it }

        val tvCpu = requireView().findViewById<TextView>(R.id.tvCpu)
        ("${Build.HARDWARE}\n$CPU_ABI ${Build.CPU_ABI2}").also { tvCpu.text = it }

        val mi = ActivityManager.MemoryInfo()
        val activityManager =
            requireContext().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        val availableMegs: Long = mi.availMem / 0x100000L
        val percentAvail: Long = mi.totalMem / 0x100000L
        val tvHardware = requireView().findViewById<TextView>(R.id.tvHardware)
        ("$availableMegs MB / $percentAvail MB").also { tvHardware.text = it }

        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val bytesAvailable: Long = stat.blockSizeLong * stat.availableBlocksLong
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val megAvailable = bytesAvailable / (1024 * 1024)
        val bytesAvailable2: Long = totalBlocks * blockSize
        val maxSize = bytesAvailable2 / (1024 * 1024)

        val memory = requireView().findViewById<TextView>(R.id.tvMemory)
        ("$megAvailable MB / $maxSize MB").also { memory.text = it }

        val battery = requireView().findViewById<TextView>(R.id.tvBattery)
        ("${getBattery(context)} mAh").also { battery.text = it }

        val mac = requireView().findViewById<TextView>(R.id.tvMac)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > 28) {
            mac.text = getString(R.string.not_support)
        } else {
            mac.text = getMac()
        }

        val tvDisplay = requireView().findViewById<TextView>(R.id.tvDisplay)
        ("${Build.DISPLAY}\n${Build.HOST}\n$SERIAL").also { tvDisplay.text = it }

        if (srlDevice.isRefreshing) {
            srlDevice.isRefreshing = false
        }
    }

    private fun getMac(): String {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            return ex.toString()
        }
        return "02:00:00:00:00:00"
    }

    @SuppressLint("PrivateApi")
    private fun getBattery(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val powerProfile = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(powerProfile)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(powerProfile)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }
}