package com.example.FARCCAndroid

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi

private lateinit var sensorManager: SensorManager

class SensorOutput(inMap:String, inOutputOffset:Float, inComponent:ProgressBar?=null,
                   inDbgTLow:TextView?=null,inDbgTMed:TextView?=null,inDbgTHigh:TextView?=null) {
    var max : Float = -100000F
    var min : Float = 100000F
    var offset : Float = 0F
    var multiplier : Float = 10F
    var value : Float = 0F
    var map : String = ""
    var outputOffset  = 0F
    var component : ProgressBar? = null
    var dbgTLow : TextView? = null
    var dbgTMed : TextView? = null
    var dbgTHigh : TextView? = null
    var rangeSkip : Float = 5000F

    var averages : MutableList<Float?> = mutableListOf()
    val maxAverages = 10
    var average = 0F

    init {
        map = inMap
        outputOffset = inOutputOffset
        component = inComponent
        dbgTLow = inDbgTLow
        dbgTMed = inDbgTMed
        dbgTHigh = inDbgTHigh
    }

    fun clear() {
        max = -100000F
        min = 100000F
        offset = 0F
        multiplier = 10F
        value = 0F
    }
}

class SensorMapping() : SensorEventListener {
    private var components : MutableList<ProgressBar?> = mutableListOf(null)
    protected var isCalibrating = false
    protected var isCalibrated = false
    var calibrationTime:Long = 10000
    var sensor : Sensor? = null

    var sensorOutput : MutableList<SensorOutput> = mutableListOf()

    private lateinit var mainHandler : Handler


    fun setComponent(index : Int, component : ProgressBar) {
        components.add(index, component)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initSensors(context:Context, type:Int) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(type)

        if (sensor != null) {
            resetCalibrationData()
            sensorManager.registerListener(this, sensor!!, SensorManager.SENSOR_DELAY_NORMAL)
            communicator!!.writeUSB("pid:1,bmr:1,bcms:0,bchsr:12,bpm:0")
        }
        mainHandler = Handler(Looper.getMainLooper())
        startusbUpdateTimer()
    }

    private fun resetCalibrationData() {
        for (i in 0..(sensorOutput.size - 1)) {
            sensorOutput[i].clear()
        }
    }

    private val usbUpdateTimer = object:Runnable {
        override fun run() {
            for (i in 0..(sensorOutput.size - 1)) {
                if (sensorOutput[i].map != "") {

                    var message = sensorOutput[i].map +":"
                    message += (sensorOutput[i].value + sensorOutput[i].outputOffset).toInt().toString()
                    communicator!!.writeUSB(message)
                }
            }
            mainHandler.postDelayed(this, 10)
        }
    }

    fun pauseusbUpdateTimer() { mainHandler.removeCallbacks(usbUpdateTimer) }

    fun resumeusbUpdateTimer() { mainHandler.post(usbUpdateTimer) }

    fun startusbUpdateTimer() {
        mainHandler = Handler(Looper.getMainLooper())
        resumeusbUpdateTimer()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        for (i in 0..(sensorOutput.size - 1)) { sensorOutput[i].averages.add(event.values[i]) }

        if (sensorOutput[0].averages.size < sensorOutput[0].maxAverages) { return }

        for (i in 0..(sensorOutput.size - 1)) {
        /*    sensorOutput[i].average = 0F
            for (j in 0..(sensorOutput[i].maxAverages - 1)) {
                sensorOutput[i].average += sensorOutput[i].averages[j]!!
            }
            sensorOutput[i].average = sensorOutput[i].average / (sensorOutput[i].maxAverages)*/
            //sensorOutput[i].averages.sortWith(Comparator.naturalOrder<Float>())
            sensorOutput[i].average = sensorOutput[i].averages.sortedWith(Comparator.naturalOrder<Float>())[4]!!
            sensorOutput[i].averages.removeAt(0)
        }

        for (i in 0..(sensorOutput.size - 1)) {
            //sensorOutput[i].value = (event.values[i] - sensorOutput[i].offset) * sensorOutput[i].multiplier
            sensorOutput[i].value = (sensorOutput[i].average - sensorOutput[i].offset) * sensorOutput[i].multiplier
            sensorOutput[i].value = sensorOutput[i].value.coerceIn(-32767F, 32767F)
            sensorOutput[i].component!!.progress = (sensorOutput[i].value).toInt()

            sensorOutput[i].dbgTLow!!.text = (sensorOutput[i].min * sensorOutput[i].multiplier).toString()
            sensorOutput[i].dbgTHigh!!.text = (sensorOutput[i].max * sensorOutput[i].multiplier).toString()
            sensorOutput[i].dbgTMed!!.text = sensorOutput[i].value.toString()
        }

        if (isCalibrating) {
            for (i in 0..(sensorOutput.size - 1)) {
                if (sensorOutput[i].average > sensorOutput[i].max)  sensorOutput[i].max = sensorOutput[i].average
                if (sensorOutput[i].average < sensorOutput[i].min)  sensorOutput[i].min = sensorOutput[i].average
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* nobody cares */ }

    public fun calibrate() {
        isCalibrating = true
        resetCalibrationData()
        calibrate.start()
    }

    public val calibrate = object : CountDownTimer(calibrationTime, calibrationTime) {
        override fun onTick(millisUntilFinished: Long) { /* do nothing */ }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onFinish() {
            isCalibrating = false
            isCalibrated = true
            for (i in 0..(sensorOutput.size - 1)) {
                sensorOutput[i].multiplier = (32767 + sensorOutput[i].rangeSkip) / (sensorOutput[i].max - sensorOutput[i].min)
                if (sensorOutput[i].max > sensorOutput[i].min) {
                    sensorOutput[i].offset = sensorOutput[i].min + (sensorOutput[i].max - sensorOutput[i].min) / 2
                } else {
                    sensorOutput[i].offset = sensorOutput[i].max + (sensorOutput[i].min - sensorOutput[i].max) / 2
                }
                sensorOutput[i].component!!.max = (sensorOutput[i].max * sensorOutput[i].multiplier).toInt()
                sensorOutput[i].component!!.min = (sensorOutput[i].min * sensorOutput[i].multiplier).toInt()
            }
        }

    }
}

/*
        Max     Min     Ofs     Should be
        0.331   0.235   0.283   0.283   (max - min) / 2 + min
        -0.022  -0.175  -0.275  (min - max) / 2 + max
        9.706   9.610   9.658   9.658
 */

/*        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorAccelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)   // TYPE_ACCELEROMETER
        val sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) //#
        val sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) //#
        val sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sensorLinearAcceleration =
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) //#
        val sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) //#
        val sensorOrientation =
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) // Reports nothing
        val sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) //#
*/
