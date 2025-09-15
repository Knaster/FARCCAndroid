package com.example.FARCCAndroid

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.SeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface FARUIUpdater {
    fun updateUI()
    fun updateFAR()

    var updatingFromData : Boolean
}
class SeekBarListener : SeekBar.OnSeekBarChangeListener {
    var callBackClass : FARUIUpdater

    constructor(inCallBackClass : MidiSettings) {
        callBackClass = inCallBackClass
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (callBackClass.updatingFromData) return
        callBackClass.updateFAR()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}

class CheckedChangeListener: CompoundButton.OnCheckedChangeListener {
    var callBackClass : FARUIUpdater? = null

    constructor(inCallBackClass : MidiSettings) {
        callBackClass = inCallBackClass
    }

    override fun onCheckedChanged(buttonView:CompoundButton, isChecked: Boolean) {
        if (callBackClass!!.updatingFromData) return
        callBackClass?.updateFAR()
    }
}

class ItemSelectedListener() : Activity(), AdapterView.OnItemSelectedListener {
    var callBackClass : FARUIUpdater? = null

    constructor(inCallBackClass : MidiSettings) : this() {
        callBackClass = inCallBackClass
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (callBackClass!!.updatingFromData) return
        callBackClass?.updateFAR()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        if (callBackClass!!.updatingFromData) return
        callBackClass?.updateFAR()
    }
}

fun sendCommandToUsb(command: String) {
    // Use coroutine or thread to avoid blocking UI
    CoroutineScope(Dispatchers.IO).launch {
        try {
            communicator!!.writeUSB(command)
           /* usbSerialPort.write(command.toByteArray(), 1000)

            // Read response (if needed)
            val buffer = ByteArray(1024)
            val len = usbSerialPort.read(buffer, 2000)
            val response = String(buffer, 0, len)

            // Update UI if needed
            withContext(Dispatchers.Main) {
                textViewOutput.append("Received: $response\n")
            }
*/
        } catch (e: Exception) {
            Log.e("USB", "Error communicating with device", e)
        }
    }
}