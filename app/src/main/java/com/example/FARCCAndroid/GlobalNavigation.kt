package com.example.FARCCAndroid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class GlobalNavigation : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_globalnavigation, container, false)

        view.findViewById<Button>(R.id.buttonGlobalConsole).setOnClickListener {
            val nextIntent = Intent(context, Console::class.java)
            startActivity(nextIntent)
        }

        view.findViewById<Button>(R.id.buttonGlobalSaveToModule).setOnClickListener {
            communicator!!.writeUSB("gsap")
        }

        return view
    }
}