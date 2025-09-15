package com.example.FARCCAndroid

import CommandList
import StringModule
import android.app.PendingIntent
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbManager
import android.widget.ArrayAdapter
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager

/*
class UsbReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        var ACTION_USB_PERMISSION :String = "com.android.example.USB_PERMISSION"
        if (action!!.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {}
        if (action!!.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")){}
        if (action!!.equals(ACTION_USB_PERMISSION)){
            if (!intent!!.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)) {
                //Authoization refused
            } else {
                //Authorization accepted
            }
        }
    }
}//end class UsbReceiver }

 */
class Communicator(inMain:MainActivity) : SerialInputOutputManager.Listener {
    var main : MainActivity? = null
    private var lastReceivedData = String()
    var updatingFromFAR = false
    var usbPort:UsbSerialPort? = null
    val UBS_WRITE_TIMEOUT = 200

    //var usbReceiver : UsbReceiver? = null

    var isConnected = false

    var deviceArray : ArrayAdapter<String>? = null

    init {
        main = inMain
        deviceArray = ArrayAdapter<String>(main!!.applicationContext, android.R.layout.simple_spinner_item)
    }

    fun addModulesIfNeeded(number:Int) {
        while (stringModules.count() < number) {
            stringModules.add(StringModule())
        }
    }

    fun processInformationReturn(receivedText:String) {
        if (receivedText=="bchsh") {
            val error = true
        }
        val commandList = CommandList(receivedText)
        if (currentUI!!.updatingFromData) return
        updatingFromFAR = true
        commandList.commands.forEach {
            when(it.command) {
                "m" -> {
                    currentStringModule = it.argument[0].toInt()
                    addModulesIfNeeded(currentStringModule)
                }
                "mc" -> {
                }
                "b", "bcu", "bmv", "bmt", "bpkp", "bpki", "bpkd", "bpie", "mfmp", "mhmp", "mrp", "mbo",
                "bmsx", "bmsi", "bppx", "bppe", "bppr", "bmf", "psf", "bmc", "sxf", "sif", "sed", "bcf",
                "bpkp", "bpki", "bkpd", "bpie", "bpme", "bhs", "bch", "bchb", "bchshr", "bchsh", "bchs5",
                "ba", "bcha", "bchsr", "bchbn" -> {
                    stringModules[currentStringModule].commandValues.put(it.command, it.argument[0])
                    //mainClass?.updateStringModuleData()
                }
                "bhsl" -> {
                    mainClass?.addSerialText("bhsl IS DEPRECATED!")
                }
                "bhsc" -> {
                    mainClass?.addSerialText("bhsc is not implemented yet!")
                    //TODO("this is where we populate the drop down of harmonic lists with their names")
                }
                "bhsd" -> {
                    stringModules[currentStringModule].harmonicData.clear()
                    for (i in 2..(it.argument.count()-1)) {
                        stringModules[currentStringModule].harmonicData.add(it.argument[i].toFloat())
                    }
                    //mainClass?.updateHarmonicTable()
                }
                "mev" -> {
                    when(it.argument[0]) {
                        "noteon" -> {
                            instrumentMaster.evNoteOn = it.argument[1]
                            instrumentMaster.cmdNoteOn.clear()
                            instrumentMaster.cmdNoteOn.addCommands(it.argument[1])
                        }
                        "noteoff" -> {
                            instrumentMaster.evNoteOff = it.argument[1]
                            instrumentMaster.cmdNoteOff.clear()
                            instrumentMaster.cmdNoteOff.addCommands(it.argument[1])
                        }
                        "cc" -> {
                            instrumentMaster.addCC(it.argument[1].toInt(), it.argument[2])
                        }
                        "pat" -> {
                            instrumentMaster.evPolyAftertouch = it.argument[1]
                            instrumentMaster.cmdPolyAftertouch.clear()
                            //instrumentMaster.cmdPolyAftertouch.addCommands("m:0:bchsh:(pressure*303)")
                            instrumentMaster.cmdPolyAftertouch.addCommands(it.argument[1])
                        }
                        "pb" -> {
                            instrumentMaster.evPitchbend = it.argument[1]
                            instrumentMaster.cmdPitchbend.clear()
                            instrumentMaster.cmdPitchbend.addCommands(it.argument[1])
                        }
                        "cat" -> {
                            instrumentMaster.evChannelAftertouch = it.argument[1]
                            instrumentMaster.cmdChannelAftertouch.clear()
                            instrumentMaster.cmdChannelAftertouch.addCommands(it.argument[1])
                        }
                        "pc" -> {
                            instrumentMaster.evProgramChange = it.argument[1]
                            instrumentMaster.cmdProgramChange.clear()
                            instrumentMaster.cmdProgramChange.addCommands(it.argument[1])
                        }
                    }
                }
                "adcr" -> {
                    // TODO - ADC Readings
                }
                "bac" -> {
                    /*
                        TODO - Bow actuator count
                        * Clear actuator list
                        * Do one 'bad' call for each actuator
                     */
                    stringModules[currentStringModule].commandValues.set("bac", it.argument[0])
                }
                "bad" -> {
                    /*
                        TODO - Bow actuator data
                        Argument[4] is name or ""
                        Argument[0] is index
                     */
                }
                "mcf" -> {
                    /*
                        TODO - Bow MIDI Configuration
                     */
                }
                "mcfc" -> {
                    /*
                        TODO - Bow MIDI Configuration Count
                        * Clear config list
                        * Do one 'mcfn' call for each config
                     */

                    instrumentMaster.midiConfigurationNames.clear()
                    instrumentMaster.midiConfigurations = it.argument[0].toInt()
                    for (i in 0..(it.argument[0].toInt() - 1)) {
                        communicator!!.writeUSB("rqi:mcfn:" + i.toString())
                    }
                }
                "mcfn" -> {
                    instrumentMaster.midiConfigurationNames.add(it.argument[0].toInt(), it.argument[1])
                }
                "mrc" -> {
                    instrumentMaster.midiChannel = it.argument[0].toInt()
                }
                "acm" -> {
                    var commands = CommandList(it.argument[1])
                    stringModules[currentStringModule].adcCommand.set(it.argument[0].toInt(), commands)
                }
                "adcr" -> {
                    stringModules[currentStringModule].adcValue.set(it.argument[0].toInt(), it.argument[1].toInt())
                }
                "psf" -> {
                    main!!.calculateTuning(it.argument[0].toFloat())
                    stringModules[currentStringModule].commandValues.put(it.command, it.argument[0])
                }
                else -> {
                    try {
                        stringModules[currentStringModule].commandValues.put(it.command, it.argument[0])
                    } catch (e:Exception) {
                        mainClass?.addSerialText("SOERR - processInformationReturn: " + e.toString())
                    }
                }
            }
        }
        updatingFromFAR = false
        if (currentUI != null) currentUI?.updateUI()
    }

    override fun onNewData(data: ByteArray?) {
        var seq2: CharSequence = java.lang.String(data)
        // If we have data from last transfer, include that
        if (lastReceivedData != "") {
            seq2 = lastReceivedData + seq2
            lastReceivedData = ""
        }
        // Split data into commandLists per \r\n
        val strings: MutableList<String> = ArrayList( seq2.toString().split("\r\n"))
        // If data doesn't terminate, remove it from further processing and keep for next data reception
        if (!seq2.endsWith("\r\n")) {
            lastReceivedData = strings.last()
            strings.remove(strings.last())
        }

        // Iterate through each commandList
        strings.forEach {
            if (it.length >= 5) {
                when (it.substring(0,5)) {
                    "[irq]" -> main!!.runOnUiThread { processInformationReturn(it.substring(5, it.length)) }
                }
                main!!.runOnUiThread { main!!.addSerialText("<si< " + it) }
            }
        }
    }

    override fun onRunError(e: java.lang.Exception?) {
        main!!.addSerialText("SOERR - onRunError: " + e.toString())
    }

    fun connectToFAR() :  Boolean {
        isConnected = false

        val manager = main!!.getSystemService(USB_SERVICE) as UsbManager
//        usbReceiver = UsbReceiver()
        val permissionIntent = PendingIntent.getBroadcast(mainClass, 0,
            Intent("com.example.FARCCAndroid.USB_PERMISSION"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
//        val filter = IntentFilter("com.example.FARCCAndroid.USB_PERMISSION")
//        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
//        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
//        registerReceiver(mainClass!!.applicationContext, usbReceiver, filter, 0)
        for (dev in manager.deviceList)
            manager.requestPermission(dev.value, permissionIntent)

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            main!!.addSerialText("No drivers detected\n")
            return false
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        if (!manager.hasPermission(driver.device)) {
            main!!.addSerialText("Got no permissions\n")
            return false
        }

        deviceArray!!.clear()
        deviceArray!!.add(availableDrivers[0].device.productName)

        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            main!!.addSerialText("Got no connection\n")
            return false
        }

        usbPort = driver.ports[0] // Most devices have just one port (port 0)
        usbPort!!.open(connection)
        usbPort!!.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        val usbIOManager = SerialInputOutputManager(usbPort, this)
        //usbIOManager!!.readTimeout = 10
        usbIOManager!!.start()

        initializeNewFAR()
        main!!.addSerialText("Connected\n")
        isConnected = true

        return true
//        startTuningTimer()
    }

    fun initializeNewFAR() {
        lastReceivedData = ""
        stringModules.clear()
        stringModules.add(StringModule())
        currentStringModule = 0
    }

    fun writeUSB(message:String) {
        if (usbPort != null) {
            usbPort!!.write((message + "\r\n").toByteArray(), UBS_WRITE_TIMEOUT)
        }
        main!!.addSerialText("<so< " + message)
        println("<so< " + message)
    }
}