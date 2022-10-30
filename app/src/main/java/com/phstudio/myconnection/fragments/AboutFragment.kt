package com.phstudio.myconnection.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.phstudio.myconnection.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvDeveloper = view.findViewById<TextView>(R.id.tvDeveloper)
        val tvApplicationDevelopment = view.findViewById<TextView>(R.id.tvApplicationDevelopment)
        val btAbout1 = view.findViewById<Button>(R.id.btAbout1)
        val btAbout2 = view.findViewById<Button>(R.id.btAbout2)
        val btAbout3 = view.findViewById<Button>(R.id.btAbout3)
        val btAbout4 = view.findViewById<Button>(R.id.btAbout4)
        val btAbout5 = view.findViewById<Button>(R.id.btAbout5)
        val btAbout6 = view.findViewById<Button>(R.id.btAbout6)
        val btAbout7 = view.findViewById<Button>(R.id.btAbout7)
        val btAbout8 = view.findViewById<Button>(R.id.btAbout8)

        tvDeveloper.setOnClickListener {
            Toast.makeText(context, getString(R.string.developer), Toast.LENGTH_SHORT).show()
        }

        tvApplicationDevelopment.setOnClickListener {
            Toast.makeText(context, getString(R.string.app_version), Toast.LENGTH_SHORT).show()
        }

        btAbout1.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.google_play))
            )
            startActivity(browserIntent)
        }

        btAbout2.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.about))
            )
            startActivity(browserIntent)
        }

        btAbout3.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_mail))
            intent.putExtra(
                Intent.EXTRA_TEXT,
                (resources.getString(R.string.email_text) + "\n" + resources.getString(
                    R.string.email_text2
                ) + "\n" + resources.getString(R.string.email_text3))
            )
            intent.type = "message/rfc822"
            startActivity(Intent.createChooser(intent, resources.getString(R.string.select_email)))
        }

        btAbout4.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.telegram))
            )
            startActivity(browserIntent)
        }

        btAbout5.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.github))
            )
            startActivity(browserIntent)
        }

        btAbout6.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.report))
            )
            startActivity(browserIntent)
        }

        btAbout7.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.privacy))
            )
            startActivity(browserIntent)
        }

        btAbout8.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.terms))
            )
            startActivity(browserIntent)
        }
    }
}