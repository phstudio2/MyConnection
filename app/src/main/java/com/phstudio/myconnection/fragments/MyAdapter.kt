package com.phstudio.myconnection.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.phstudio.myconnection.R

class MyAdapter(private val context: Context, private val arrayList: java.util.ArrayList<MyData>) :
    BaseAdapter() {
    private lateinit var tvName: TextView
    private lateinit var tvFrequency: TextView
    private lateinit var tvChannel: TextView
    private lateinit var tvStrength: TextView
    private lateinit var tvBssid: TextView
    private lateinit var tvCap: TextView

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @Suppress("NAME_SHADOWING", "ViewHolder")
    override fun getView(position: Int, contextV: View?, parent: ViewGroup): View {
        val contextV: View? =
            LayoutInflater.from(context).inflate(R.layout.row_wifiscanner, parent, false)
        tvName = contextV!!.findViewById(R.id.tvName)
        tvFrequency = contextV.findViewById(R.id.tvFrequency)
        tvChannel = contextV.findViewById(R.id.tvChannel)
        tvStrength = contextV.findViewById(R.id.tvStrength)
        tvBssid = contextV.findViewById(R.id.tvBssid)
        tvCap = contextV.findViewById(R.id.tvAccess)

        tvName.text = arrayList[position].tvName
        tvFrequency.text = arrayList[position].tvFrequency
        tvChannel.text = arrayList[position].tvChannel
        tvStrength.text = arrayList[position].tvStrength
        tvBssid.text = arrayList[position].tvBssid
        tvCap.text = arrayList[position].tvCap

        return contextV
    }
}