package com.example.FARCCAndroid

import Helpers
import InstrumentMaster
import StringModule
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.BufferedReader
import java.io.InputStreamReader


val helpers = Helpers()
var stringModules = mutableListOf<StringModule>()
var instrumentMaster = InstrumentMaster()
var currentStringModule = -1
var communicator:Communicator? = null
var sensorMapping:SensorMapping? = null

var mainClass: MainActivity? = null

var currentUI: FARUIUpdater? = null

class LogArrayAdapter(
    context: Context,
    private val logs: List<String>
) : ArrayAdapter<String>(context,  R.layout.listitem_console, logs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val logText = logs[position]

        val background = when {
            logText.contains("[cmd]") -> "#B4FF78".toColorInt() // light red
            logText.contains("[dbg]") -> "#FFFF73".toColorInt() // light red
            logText.contains("[err]") -> "#FF0000".toColorInt() // light red
            logText.contains("[pri]") -> "#FF6464".toColorInt() // light red
            logText.contains("[hlp]") -> "#73C8FF".toColorInt() // light red
            logText.contains("[irq]") -> "#D7C8FF".toColorInt() // light red
            logText.contains("[txi]") -> "#C8C8FF".toColorInt() // light red
            logText.contains("<so<") -> "#808080".toColorInt() // light red
            else -> Color.WHITE
        }

        logs[position].substring(4, logs[position].length)

        view.setBackgroundColor(background)

        return view
    }
}
var logArray: ArrayList<String>? = null
var logAdapter: LogArrayAdapter? = null // ArrayAdapter<String>? = null
var logMaxSize = 500

fun hideSystemUI(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(
        window,
        window.decorView.findViewById(android.R.id.content)
    ).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // When the screen is swiped up at the bottom
        // of the application, the navigationBar shall
        // appear for some time
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
class MainActivity  : AppCompatActivity(), FARUIUpdater  {
//class MainActivity : CompoundButton.OnCheckedChangeListener, ComponentActivity()  {
    lateinit var mainHandler : Handler
    override var updatingFromData : Boolean = false

    private val tuningTimer = object:Runnable {
        override fun run() {
            communicator!!.writeUSB("rqi:psf")
            mainHandler.postDelayed(this, 100)
        }
    }

    fun pauseTuningTimer() { mainHandler.removeCallbacks(tuningTimer) }

    fun resumeTuningTimer() { mainHandler.post(tuningTimer) }

    fun startTuningTimer() {
        mainHandler = Handler(Looper.getMainLooper())
        resumeTuningTimer()
    }

    override fun onDestroy() {
//        usbPort!!.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainClass = this
        currentUI = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.intropage)
        hideSystemUI(this.window)

        findViewById<SwitchCompat>(R.id.switchConnect).setOnClickListener {
            if (findViewById<SwitchCompat>(R.id.switchConnect).isChecked) {
                if (!communicator!!.connectToFAR()) {
                    findViewById<SwitchCompat>(R.id.switchConnect).isChecked = false
                }
            }
        }

        findViewById<Button>(R.id.buttonConsole).setOnClickListener {
            val nextIntent = Intent(this, Console::class.java)
            startActivity(nextIntent)
        }

        findViewById<Button>(R.id.buttonMIDISettings).setOnClickListener {
            val nextIntent = Intent(this@MainActivity, MidiSettings::class.java)
            startActivity(nextIntent)
        }

        findViewById<Button>(R.id.buttonXYPad).setOnClickListener {
            val nextIntent = Intent(this@MainActivity, XYController::class.java)
            startActivity(nextIntent)
        }

        findViewById<Button>(R.id.buttonOvertoneMap).setOnClickListener {
            val nextIntent = Intent(this@MainActivity, HarmonicEditor::class.java)
            startActivity(nextIntent)
        }

        findViewById<Button>(R.id.buttonSensorControl).setOnClickListener {
            val nextIntent = Intent(this@MainActivity, SensorActivity::class.java)
            startActivity(nextIntent)
        }

        findViewById<Button>(R.id.buttonAppSettings).setOnClickListener {
            communicator?.initializeNewFAR()
            val assetManager = getAssets()
            val inputStream = assetManager.open("serialtest.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String

            while ((reader.readLine().also { line = it }) != null) {
                if (line.length >= 5) {
                    when (line?.substring(0,5)) {
                        "[irq]" ->  { communicator!!.processInformationReturn(line.substring(5, line.length)) }
                    }
                    addSerialText("<si< " + line)
                }
            }
            reader.close()
        }
        logArray = ArrayList<String>()
        logAdapter = LogArrayAdapter(applicationContext,logArray!!)

        communicator = Communicator(this)
        findViewById<Spinner>(R.id.spinnerMainDevices).adapter = communicator!!.deviceArray
        communicator!!.connectToFAR()
        if (communicator!!.isConnected) {
            findViewById<SwitchCompat>(R.id.switchConnect).isChecked = true
        } else {
            findViewById<SwitchCompat>(R.id.switchConnect).isChecked = false
        }
    }

    override fun updateUI() {

    }

    override fun updateFAR() {

    }

    fun convertToHtml(input: String): String {
        val htmlMap = mapOf(
            "&" to "&amp;",
            "<" to "&lt;",
            ">" to "&gt;",
            "\"" to "&quot;",
            "'" to "&#39;"
        )

        var output = input
        for ((key, value) in htmlMap) {
            output = output.replace(key, value)
        }
        return output
    }

    fun addSerialText(text: String) {
        if (logArray!!.size > logMaxSize) logArray!!.removeAt(0)
        logArray!!.add(text)
        logAdapter!!.notifyDataSetChanged()
    }

    fun requestStringModuleData() {
        communicator!!.writeUSB("rqi:mc")
        communicator!!.writeUSB("rqi:m")
        communicator!!.writeUSB("rqi:bcf")
        communicator!!.writeUSB("rqi:bowcontrolfundamental")
        communicator!!.writeUSB("rqi:bmv")
        communicator!!.writeUSB("rqi:bowpidki")
        communicator!!.writeUSB("rqi:bowpidkp")
        communicator!!.writeUSB("rqi:bowpidkd")
        communicator!!.writeUSB("rqi:bowpidintegratorerror")
        communicator!!.writeUSB("rqi:bowmotortimeout")
        communicator!!.writeUSB("rqi:mutefullmuteposition")
        communicator!!.writeUSB("rqi:mutehalfmuteposition")
        communicator!!.writeUSB("rqi:muterestposition")
        communicator!!.writeUSB("rqi:mutebackoff")
        communicator!!.writeUSB("rqi:bowmotorspeedmax")
        communicator!!.writeUSB("rqi:bowmotorspeedmin")
        communicator!!.writeUSB("rqi:bowpressurepositionmax")
        communicator!!.writeUSB("rqi:bowpressurepositionengage")
        communicator!!.writeUSB("rqi:bowpressurepositionrest")

        communicator!!.writeUSB("rqi:bowcontrolharmonic")
        communicator!!.writeUSB("rqi:bowcontrolharmonicbase")
        communicator!!.writeUSB("rqi:bowcontrolharmonicbasenote")
        communicator!!.writeUSB("rqi:bowcontrolharmonicadd")
        communicator!!.writeUSB("rqi:bowcontrolharmonicshift")
        communicator!!.writeUSB("rqi:bowcontrolharmonicshiftrange")
        communicator!!.writeUSB("rqi:bowcontrolharmonicshift5")
        communicator!!.writeUSB("rqi:bowcontrolfrequency")

        communicator!!.writeUSB("rqi:bowharmonicseriescount")
        communicator!!.writeUSB("rqi:bowharmonicseries")

        communicator!!.writeUSB("rqi:midiconfigurationcount")
        communicator!!.writeUSB("rqi:midiconfiguration")

        communicator!!.writeUSB("rqi:bowcontrolharmonicbasenote")
        communicator!!.writeUSB("rqi:solenoidmaxforce")
        communicator!!.writeUSB("rqi:solenoidminforce")
        communicator!!.writeUSB("rqi:solenoidengageduration")

        communicator!!.writeUSB("rqi:bowactuator")
        communicator!!.writeUSB("rqi:bowactuatorcount")

        communicator!!.writeUSB("rqi:mrc")
        communicator!!.writeUSB("rqi:acm:0")
        communicator!!.writeUSB("rqi:acm:1")
        communicator!!.writeUSB("rqi:acm:2")
        communicator!!.writeUSB("rqi:acm:3")
        communicator!!.writeUSB("rqi:acm:4")
        communicator!!.writeUSB("rqi:acm:5")
        communicator!!.writeUSB("rqi:acm:6")
        communicator!!.writeUSB("rqi:acm:7")

        communicator!!.writeUSB("rqi:bpkp")
        communicator!!.writeUSB("rqi:bpki")
        communicator!!.writeUSB("rqi:bpkd")
        communicator!!.writeUSB("rqi:bpie")
        communicator!!.writeUSB("rqi:bpme")


    }

    fun calculateTuning(frequency:Float) {
        if (frequency <= 0) return
        val ret = helpers.getBaseNoteFromFrequency(frequency, helpers.scaleDataJust)
        /*findViewById<TextView>(R.id.tuningHertzTextView).text = frequency.toString() + " Hz"
        findViewById<TextView>(R.id.tuningNoteTextView).text = ret[3] + ret[0]
        findViewById<TextView>(R.id.tuningCentsTextView).text = round(ret[2].toFloat()).toString()
        findViewById<SeekBar>(R.id.tuningCentsScrollBar).progress = round(60 + ret[2].toFloat()).toInt()
         */
    }
}