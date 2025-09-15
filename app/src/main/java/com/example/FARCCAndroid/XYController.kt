package com.example.FARCCAndroid

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.time.TimeSource

class XYController : AppCompatActivity() {
    var xMult:Int = 0
    var yMult:Int = 0
    lateinit var mainHandler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.xycontrol)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.XYControlMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUI(this.window)

        val constraintLayout = findViewById<View>(R.id.XYControlMain)
        constraintLayout.isClickable = true
        constraintLayout.isFocusable = true
        constraintLayout.setOnTouchListener(OnTouchListener { v, event ->
            //v.performClick()
            return@OnTouchListener touchListener(v, event)
        })

    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        findViewById<View>(R.id.harmonicEditMain).setOnTouchListener(OnTouchListener { v, event->
            return@OnTouchListener true
        })
    }

    var touchHold = false
    var touchTimerFinished = false
    var touchView:View? = null
    var touchElapse = false

    private val touchTimer = object:CountDownTimer(100,100) {
        /*        override fun run() {
                    mainHandler.postDelayed(this, 100)
                }*/
        override fun onTick(millisUntilFinished: Long) {
            return
        }
        override fun onFinish() {
            touchHold = !touchHold
            touchTimerFinished = true
            if (touchHold) {
                touchView!!.setBackgroundColor(Color.rgb(255,127,127))
                communicator!!.writeUSB("bph:0")
            } else {
                touchView!!.setBackgroundColor(Color.rgb(255,255,255))
                communicator!!.writeUSB("bph:1")
            }
        }

        init {
            touchTimerFinished = false
        }
    }

    val timeSource = TimeSource.Monotonic
    var markDown = timeSource.markNow()

    fun touchListener(v: View, event: MotionEvent):Boolean {
        var result = "X " + event.x.toString() + " : Y " + event.y.toString()

        var message = ""
        if (event.action == MotionEvent.ACTION_UP) {
            result += " - UP"
            message = "bpr:1"
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            touchTimerFinished = false
            touchTimer.start()
            v.setBackgroundColor(Color.rgb(255,0,0))
            touchView = v
            return false
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {

            if (!touchTimerFinished) {
                touchTimer.cancel()
                message = "se:65535"
            }
        } else {
            if (event.action == MotionEvent.ACTION_DOWN) {
                result += " - DOWN"
                message = "bpe:1,"
            }

            xMult = (65535 / v.width)
            yMult = (65535 / v.height)

            var pressure:Int = 65535 - (event.y * yMult).toInt() - 2000
            if (pressure < 0) pressure = 0
            if (pressure > 655535) pressure = 655535

            var shift:Int = ((event.x) * xMult).toInt() - 32767
            if (shift < -32767) shift = -32767
            if (shift > 32767) shift = 32767

            message += "pid:1,bmr:1,bcms:0,bchsr:12,bpm:" + pressure.toString() + ",bchsh:" + shift.toString()

            val touchPoint = findViewById<ImageView>(R.id.touchPoint)
            touchPoint.x = event.x - (touchPoint.width / 2)
            touchPoint.y = event.y - (touchPoint.height / 2)
        }

        if (touchHold) {
            v.setBackgroundColor(Color.rgb(255,127,127))
        } else {
            v.setBackgroundColor(resources.getColor(R.color.widget_background, theme))
        }

        findViewById<TextView>(R.id.coordinatesTextView).text = result
        communicator!!.writeUSB(message)
        return true
    }
}