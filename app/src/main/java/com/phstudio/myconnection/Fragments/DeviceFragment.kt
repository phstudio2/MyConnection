package com.phstudio.myconnection.Fragments

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
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
        val swipetorefresh =
            requireView().findViewById<SwipeRefreshLayout>(R.id.swipetorefreshdevice)
        swipetorefresh.setOnRefreshListener {
            show()
        }
        show()

    }

    @SuppressLint("HardwareIds")
    private fun show() {
        val swipetorefresh =
            requireView().findViewById<SwipeRefreshLayout>(R.id.swipetorefreshdevice)
        val _RELEASE = Build.VERSION.RELEASE
        val _MODEL = Build.MODEL
        val _PRODUCT = Build.PRODUCT
        val _BRAND = Build.BRAND
        val _CPU_ABI = Build.CPU_ABI
        val _CPU_ABI2 = Build.CPU_ABI2
        val _HARDWARE = Build.HARDWARE
        val _DISPLAY = Build.DISPLAY

        val _SERIAL = Build.SERIAL
        val _HOST = Build.HOST

        val brand = requireView().findViewById<TextView>(R.id.brand)
        brand.text = (_BRAND)

        val model = requireView().findViewById<TextView>(R.id.model)
        model.text = ("$_MODEL ($_PRODUCT)")

        val version = requireView().findViewById<TextView>(R.id.version)
        version.text = ("$_RELEASE (API: ${(Build.VERSION.SDK_INT)})")

        val cpu = requireView().findViewById<TextView>(R.id.cpu)
        cpu.text = ("$_HARDWARE\n$_CPU_ABI $_CPU_ABI2")

        val mi = ActivityManager.MemoryInfo()
        val activityManager =
            requireContext().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        val availableMegs: Long = mi.availMem / 0x100000L
        val percentAvail: Long = mi.totalMem / 0x100000L
        val hardware = requireView().findViewById<TextView>(R.id.hardware)

        hardware.text = ("$availableMegs MB / $percentAvail MB")

        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val bytesAvailable: Long = stat.blockSizeLong * stat.availableBlocksLong
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val megAvailable = bytesAvailable / (1024 * 1024)
        val bytesAvailable2: Long = totalBlocks * blockSize
        val maxSize = bytesAvailable2 / (1024 * 1024)

        val memory = requireView().findViewById<TextView>(R.id.memory)
        memory.text = ("$megAvailable MB / $maxSize MB")

        val battery = requireView().findViewById<TextView>(R.id.battery)
        battery.text = (getBatteryCapacity(context).toString() + " mAh")

        val mac = requireView().findViewById<TextView>(R.id.mac)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > 28) {
            mac.text = ("\n" + getString(R.string.notsupport))
        } else {
            mac.text = getMacAddr()
        }

        val display = requireView().findViewById<TextView>(R.id.display)
        display.text = (_DISPLAY + "\n" + _HOST + "\n" + _SERIAL)
        if (swipetorefresh.isRefreshing) {
            swipetorefresh.isRefreshing = false
        }
    }

    fun getMacAddr(): String {
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
        }
        return "02:00:00:00:00:00"
    }

    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }

}