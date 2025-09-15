package com.example.FARCCAndroid

import android.hardware.Sensor
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SensorActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sensor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.harmonicEditMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorMapping = SensorMapping()
        sensorMapping!!.sensorOutput.add(0, SensorOutput("bchsh", 0F, findViewById<ProgressBar>(R.id.a1Progress),
            findViewById<TextView>(R.id.tvLow0), findViewById<TextView>(R.id.tvMed0), findViewById<TextView>(R.id.tvHigh0)))
        sensorMapping!!.sensorOutput.add(1, SensorOutput("bpm", -32767F, findViewById<ProgressBar>(R.id.a0Progress),
            findViewById<TextView>(R.id.tvLow1), findViewById<TextView>(R.id.tvMed1), findViewById<TextView>(R.id.tvHigh1)))
        sensorMapping!!.sensorOutput.add(2, SensorOutput("", 0F, findViewById<ProgressBar>(R.id.a2Progress),
            findViewById<TextView>(R.id.tvLow2), findViewById<TextView>(R.id.tvMed2), findViewById<TextView>(R.id.tvHigh2)))

        sensorMapping!!.initSensors(applicationContext, Sensor.TYPE_ACCELEROMETER)

        findViewById<Button>(R.id.calibrateButton).setOnClickListener() {
            sensorMapping!!.calibrate()
        }
    }
}