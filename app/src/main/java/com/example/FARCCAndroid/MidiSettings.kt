package com.example.FARCCAndroid

import CommandItem
import CommandList
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import getVariable
import isVariableInEquation


class MidiSettings: AppCompatActivity(), FARUIUpdater {
    var eventCommandsAdapter : ArrayAdapter<String>? = null
    var created = false
    override var updatingFromData : Boolean = false

    override fun onResume() {
        super.onResume()
        if (!created) return
        currentUI = this
        requestStringModuleData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUI = this

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_midisettings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.MIDISettingsMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUI(this.window)
        updatingFromData = true

        ArrayAdapter.createFromResource(this, R.array.continuousMappables,
            android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                findViewById<Spinner>(R.id.spinnerMIDIPitchBendTarget).adapter = adapter
                findViewById<Spinner>(R.id.spinnerMIDIPolyAftertouchTarget).adapter = adapter
                findViewById<Spinner>(R.id.spinnerMIDIChannelAftertouchTarget).adapter = adapter
        }

        ArrayAdapter.createFromResource(this, R.array.booleanMappables,
            android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                findViewById<Spinner>(R.id.spinnerMIDISustainTarget).adapter = adapter
        }

        findViewById<Spinner>(R.id.spinnerMIDIPitchBendTarget).setSelection(0, false)
        findViewById<Spinner>(R.id.spinnerMIDIPolyAftertouchTarget).setSelection(0, false)
        findViewById<Spinner>(R.id.spinnerMIDIChannelAftertouchTarget).setSelection(0, false)
        findViewById<Spinner>(R.id.spinnerMIDISustainTarget).setSelection(0, false)

        findViewById<Spinner>(R.id.spinnerMIDIConfiguration).adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerMIDIConfiguration).setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,view: View?, position: Int, id: Long) {
                if (!updatingFromData) {
                    communicator!!.writeUSB("mcf:" + parent!!.selectedItem)
                    communicator!!.writeUSB("rqi:mev")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        findViewById<Button>(R.id.buttonMIDIConfigurationAdd).setOnClickListener {
            val d : Dialog = Dialog(this)
            d.setTitle("Add configuration")
            d.setContentView(R.layout.dialog_textedit)
            d.show()

            d.findViewById<Button>(R.id.dialogTextEditOk).setOnClickListener {
                communicator!!.writeUSB("mcfa:" + d.findViewById< EditText>(R.id.dialogTextEditEdit).text +
                    ", rqi:mcfn")
                d.dismiss()
            }

            d.findViewById<Button>(R.id.dialogTextEditCancel).setOnClickListener {
                d.dismiss()
            }
        }

        findViewById<Button>(R.id.buttonMIDIConfigurationRemove).setOnClickListener {
            val midiconf = findViewById<Spinner>(R.id.spinnerMIDIConfiguration)
            if (midiconf.count > 1) {
                communicator!!.writeUSB("mcfr:" + midiconf.selectedItemPosition.toInt())
            }
        }

        findViewById<Button>(R.id.buttonMIDIConfigurationRename).setOnClickListener {
            val midiconf = findViewById<Spinner>(R.id.spinnerMIDIConfiguration)

            if (midiconf.selectedItem != null) {
                //communicator!!.writeUSB("mcfr:" + midiconf.selectedItemPosition.toInt())
                // TODO pop up name dialog
                val d : Dialog = Dialog(this)
                d.setTitle("Rename configuration")
                d.setContentView(R.layout.dialog_textedit)
                d.show()

                d.findViewById<Button>(R.id.dialogTextEditOk).setOnClickListener {
                    communicator!!.writeUSB("mcf:" + midiconf.selectedItemPosition.toString() + ", mcfn:" +
                            d.findViewById< EditText>(R.id.dialogTextEdit).text)
                    d.dismiss()
                }

                d.findViewById<Button>(R.id.dialogTextEditCancel).setOnClickListener {
                    d.dismiss()
                }

            }
        }

        val midiChannel = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerMIDIChannel).adapter = midiChannel
        midiChannel.add("OMNI")
        for (i in 1..16) {
            midiChannel.add(i.toString())
        }

        val seekBarListener = SeekBarListener(this)
        val checkedChangeListener = CheckedChangeListener(this)
        val itemSelectedListener = ItemSelectedListener(this)
        findViewById<Spinner>(R.id.spinnerMIDIChannel).onItemSelectedListener = itemSelectedListener
        findViewById<SeekBar>(R.id.seekbarMIDINoteOnVelocityToHammer).setOnSeekBarChangeListener(seekBarListener)
        findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendHammerOnNewNotes).setOnCheckedChangeListener(checkedChangeListener)
        findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendMuteRest).setOnCheckedChangeListener(checkedChangeListener)
        findViewById<CheckBox>(R.id.checkboxMIDINoteOffSendFullMute).setOnCheckedChangeListener(checkedChangeListener)
        findViewById<CheckBox>(R.id.checkboxMIDINoteOffTurnOffMotor).setOnCheckedChangeListener(checkedChangeListener)
        findViewById<Spinner>(R.id.spinnerMIDIPitchBendTarget).onItemSelectedListener = itemSelectedListener
        findViewById<SeekBar>(R.id.seekbarMIDIPitchBendScale).setOnSeekBarChangeListener(seekBarListener)
        findViewById<CheckBox>(R.id.checkboxMIDISustainInvert).setOnCheckedChangeListener(checkedChangeListener)
        findViewById<Spinner>(R.id.spinnerMIDIPolyAftertouchTarget).onItemSelectedListener = itemSelectedListener
        findViewById<SeekBar>(R.id.seekbarMIDIPolyAftertouchScale).setOnSeekBarChangeListener(seekBarListener)
        findViewById<Spinner>(R.id.spinnerMIDIChannelAftertouchTarget).onItemSelectedListener = itemSelectedListener
        findViewById<SeekBar>(R.id.seekbarMIDIChannelAftertouchScale).setOnSeekBarChangeListener(seekBarListener)

        //findViewById<Spinner>(R.id.spinnerMIDIChannelAftertouchTarget).adapter.

        eventCommandsAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerMIDIAdvancedEvent).adapter = eventCommandsAdapter
        updatingFromData = false

        findViewById<Spinner>(R.id.spinnerMIDIAdvancedEvent).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long ) {
                if (updatingFromData) return
                updatingFromData = true

                val eventText = findViewById<TextInputEditText>(R.id.texteditMIDIAdvancedEditInput)
                val itemText = findViewById<Spinner>(R.id.spinnerMIDIAdvancedEvent).selectedItem.toString()
                when(itemText) {
                    "Note On" -> {
                        eventText.setText(instrumentMaster.evNoteOn)
                    }
                    "Note Off" -> {
                        eventText.setText(instrumentMaster.evNoteOff)
                    }
                    "Poly aftertouch" -> {
                        eventText.setText(instrumentMaster.evPolyAftertouch)
                    }
                    "Channel aftertouch" -> {
                        eventText.setText(instrumentMaster.evChannelAftertouch)
                    }
                    "Pitchbend" -> {
                        eventText.setText(instrumentMaster.evPitchbend)
                    }
                    "Program change" -> {
                        eventText.setText(instrumentMaster.evProgramChange)
                    }
                    else -> {
                        if (itemText.subSequence(0,2) == "CC") {
                            val control = itemText.subSequence(3, itemText.length).toString()
                            eventText.setText(instrumentMaster.getCC(control.toInt()))
                        }
                    }
                }
                updatingFromData = false
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                findViewById<TextInputEditText>(R.id.texteditMIDIAdvancedEditInput).setText("")
            }
        }

        findViewById<TextInputEditText>(R.id.texteditMIDIAdvancedEditInput).onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean): Unit {
                if (!hasFocus) eventCommandStringUpdate()
            }
        }

        findViewById<TextInputEditText>(R.id.texteditMIDIAdvancedEditInput).setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        eventCommandStringUpdate()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        })

        findViewById<Button>(R.id.buttonMIDIAdvancedAddCC).setOnClickListener {
            val d : Dialog = Dialog(this)
            d.setTitle("Add CC")
            //LayoutInflater.from(this).inflate(R.layout.numberpickercc, null, true)
            d.setContentView(R.layout.dialog_numberpickercc)
            val np = d.findViewById<NumberPicker>(R.id.numberPickerDialog)
            np.maxValue = 127
            np.minValue = 0
            np.wrapSelectorWheel = true
            d.show()

            val b1 = d.findViewById<Button>(R.id.numberPickerSetButton)
            val b2 = d.findViewById<Button>(R.id.numberPickerCancelButton)
            b1.setOnClickListener {
                //instrumentMaster.addCC(np.value, "")
                //updateFAR()
                //updateUI()
                communicator!!.writeUSB("mev:cc:" + np.value.toString() + ":''")
                d.dismiss()
            }
            b2.setOnClickListener {
                d.dismiss()
            }
        }

        findViewById<Button>(R.id.buttonMIDIAdvancedRemoveCC).setOnClickListener {
            val itemText = findViewById<Spinner>(R.id.spinnerMIDIAdvancedEvent).selectedItem.toString()
            if (itemText.subSequence(0,2) == "CC") {
                val control = itemText.subSequence(3, itemText.length).toString().toString()
                communicator!!.writeUSB("mevcr:" + control)
                //instrumentMaster.removeCC(control)
                //updateFAR()
                //updateUI()

            }
        }

        findViewById<Button>(R.id.buttonMIDIRestoreDefaults).setOnClickListener {
            val d : Dialog = Dialog(this)
            d.setTitle("Restore MIDI Defaults")
            d.setContentView(R.layout.dialog_twobuttons)
            d.show()

            val b1 = d.findViewById<Button>(R.id.dialogButton1)
            val b2 = d.findViewById<Button>(R.id.dialogButton2)
            b1.setOnClickListener {
                communicator!!.writeUSB("mcfd")
                //mainClass?.requestStringModuleData()
                //updateUI()
                d.dismiss()
            }
            b2.setOnClickListener {
                d.dismiss()
            }
        }

        //updateUI()
        created = true
        requestStringModuleData()
    }

    fun requestStringModuleData() {
        communicator!!.writeUSB("rqi:mcfc")
        communicator!!.writeUSB("rqi:mcf")
        communicator!!.writeUSB("rqi:mev")
    }

    fun eventCommandStringUpdate() {
        if (updatingFromData) return

        val eventText = findViewById<TextInputEditText>(R.id.texteditMIDIAdvancedEditInput)
        val itemText = findViewById<Spinner>(R.id.spinnerMIDIAdvancedEvent).selectedItem.toString()
        when(itemText) {
            "Note On" -> {
                //instrumentMaster.evNoteOn = eventText.text.toString()
                communicator!!.writeUSB("mev:noteon:'" + eventText.text.toString() + "'")
            }
            "Note Off" -> {
//                instrumentMaster.evNoteOff = eventText.text.toString()
                communicator!!.writeUSB("mev:noteoff:'" + eventText.text.toString() + "'")
            }
            "Poly aftertouch" -> {
//                instrumentMaster.evPolyAftertouch  = eventText.text.toString()
                communicator!!.writeUSB("mev:pat:'" + eventText.text.toString() + "'")
            }
            "Channel aftertouch" -> {
//                instrumentMaster.evChannelAftertouch  = eventText.text.toString()
                communicator!!.writeUSB("mev:cat:'" + eventText.text.toString() + "'")
            }
            "Pitchbend" -> {
//                instrumentMaster.evPitchbend  = eventText.text.toString()
                communicator!!.writeUSB("mev:pb:'" + eventText.text.toString() + "'")
            }
            "Program change" -> {
//                instrumentMaster.evProgramChange = eventText.text.toString()
                communicator!!.writeUSB("mev:pc:'" + eventText.text.toString() + "'")
            }
            else -> {
                if (itemText.subSequence(0,2) == "CC") {
                    val control = itemText.subSequence(3, itemText.length).toString()
                    //instrumentMaster.addCC(control.toInt(), eventText.text.toString())
                    communicator!!.writeUSB("mev:cc:" + control + ":'" + eventText.text.toString() + "'")
                }
            }
        }
        //updateFAR()
        //updateUI()
    }

    override fun updateUI() {
        updatingFromData = true
        eventCommandsAdapter?.clear()

        val configs = findViewById<Spinner>(R.id.spinnerMIDIConfiguration).adapter as ArrayAdapter<String>
        configs.clear()
        for (i in 0..instrumentMaster.midiConfigurationNames.count()-1) {
            configs.add(instrumentMaster.midiConfigurationNames[i])
        }

        val midiChannel = findViewById<Spinner>(R.id.spinnerMIDIChannel)
        if ((instrumentMaster.midiChannel > 1) && (instrumentMaster.midiChannel < 17)) {
            midiChannel.setSelection(instrumentMaster.midiChannel, false)
        } else {
            midiChannel.setSelection(0, false)
        }

        var sev = getVariable(instrumentMaster.cmdNoteOn.getCommandAttribute("se",0), "velocity")
        findViewById<SeekBar>(R.id.seekbarMIDINoteOnVelocityToHammer).setProgress(sev.first.toInt(), false)
        findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendHammerOnNewNotes).isChecked =
            isVariableInEquation(instrumentMaster.cmdNoteOn.getCommandAttribute("se", 0), "notecount")
        findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendMuteRest).isChecked =
            (instrumentMaster.cmdNoteOn.getCommandAttribute("mr",0) != "")
        eventCommandsAdapter?.add("Note On")

        findViewById<CheckBox>(R.id.checkboxMIDINoteOffSendFullMute).isChecked =
            (instrumentMaster.cmdNoteOff.getCommandAttribute("mfm", 0) != "")
        findViewById<CheckBox>(R.id.checkboxMIDINoteOffTurnOffMotor).isChecked =
            (instrumentMaster.cmdNoteOff.getCommandAttribute("bmr", 0) != "")
        eventCommandsAdapter?.add("Note Off")

        for (i in instrumentMaster.evCC) {
            eventCommandsAdapter?.add("CC " + i.control.toString())
        }

        selectSendDestinationAndRatio(R.id.spinnerMIDIPolyAftertouchTarget,
            instrumentMaster.cmdPolyAftertouch, R.id.seekbarMIDIPolyAftertouchScale,
            "pressure",1)
        eventCommandsAdapter?.add("Poly aftertouch")

        selectSendDestinationAndRatio(R.id.spinnerMIDIPitchBendTarget,
            instrumentMaster.cmdPitchbend, R.id.seekbarMIDIPitchBendScale,
            "pitch",127)
        eventCommandsAdapter?.add("Pitchbend")

        selectSendDestinationAndRatio(R.id.spinnerMIDIChannelAftertouchTarget,
            instrumentMaster.cmdChannelAftertouch, R.id.seekbarMIDIChannelAftertouchScale,
            "pressure",1)
        eventCommandsAdapter?.add("Channel aftertouch")

        eventCommandsAdapter?.add("Program change")

        val sustain = instrumentMaster.getCC(64)
        val sustainView = findViewById<Spinner>(R.id.spinnerMIDISustainTarget)
        if (sustain != null) {
            val cl = CommandList(sustain)

            if (cl.commands.count() == 0)
                sustainView.setSelection(0)
            else if (cl.commands.count() == 2)
                sustainView.setSelection(1)
            else if (cl.commands.count() == 1) {
                if (cl.commands[0].command == "bph")
                    sustainView.setSelection(2)
                else if (cl.commands[0].command == "ms")
                    sustainView.setSelection(3)
                else
                    sustainView.setSelection(0)
                }
            else
                sustainView.setSelection(0)

            if (cl.commands.count() != 0) {
                val invertBox = findViewById<CheckBox>(R.id.checkboxMIDISustainInvert)
                if (cl.commands[0].argument[0].contains("ibool")) {
                    invertBox.isChecked = true
                } else {
                    invertBox.isChecked = false
                }
            }
        } else sustainView.setSelection(0)

        updatingFromData = false
    }

    fun getContinuousMapAssociation(map : String) : String {
        when(map) {
            "Pressure modifier" -> return "bpm"
            "Pressure baseline" -> return "bpb"
            "Mute position" -> return "msp"
            "Solenoid force multiplier" -> return "sfm"
            "Harmonic shift" -> return "bchsh"
        }
        return ""
    }

    fun getBooleanMapAssociation(map : String) : Array<String>? {
        when(map) {
            "" -> return null
            "MIDI & Mute sustain" -> return arrayOf("bph", "ms")
            "MIDI sustain" -> return arrayOf("bph")
            "Mute sustain" -> return arrayOf("ms")
        }
        return null
    }

    fun selectSendDestinationAndRatio(spinnerID : Int, commandList: CommandList, seekbarID : Int,
                                      variable : String, inMultiplier : Int) {
        var found = false
        var g : CommandItem? = null
        val spinner = findViewById<Spinner>(spinnerID)
        val ratio = findViewById<SeekBar>(seekbarID)

        for (a in commandList.commands)
            for (b in 0..spinner.adapter.count-1)
                if (getContinuousMapAssociation(spinner.adapter.getItem(b).toString()) ==
                    a.command) {
                    found = true
                    if (!updatingFromData) {
                        val apa = 5
                    }
                    updatingFromData = true
                    spinner.setSelection(b, false)
                    updatingFromData = false
                    g = a
                    break
                }


        if (found)
            try {
                val result = getVariable(g!!.argument[0], variable)
                val multiplier : Int = (result.first * inMultiplier).toInt()
                updatingFromData = true
                if (!updatingFromData) {
                    val apa = 5
                }
                ratio.setProgress(multiplier, false)
                updatingFromData = false
            } catch(e : Exception) {
                val eop = 10
            }
    }

    override fun updateFAR() {
        if (communicator!!.updatingFromFAR) return

        var midiChannel : String? = findViewById<Spinner>(R.id.spinnerMIDIChannel).selectedItem.toString()
        if (midiChannel != null) {
            if (midiChannel == "OMNI") {
                midiChannel = "0"
            }
            communicator!!.writeUSB("mrc:" + midiChannel.toString())
        }

        var cl = CommandList(instrumentMaster.evNoteOn)
        var cmd = cl.buildCommandString(arrayOf<String>("se","mr"))
        
        val velToHammer = findViewById<SeekBar>(R.id.seekbarMIDINoteOnVelocityToHammer).progress
        if (velToHammer > 0) {
            cmd += "se:(velocity*" + velToHammer.toString() + ")"
            if (findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendHammerOnNewNotes).isChecked)
                cmd += "*(1-notecount)"
        }
        if (findViewById<CheckBox>(R.id.checkboxMIDINoteOnSendMuteRest).isChecked)
            cmd += ",mr:1"
        var serialString = "mev:noteon:\"$cmd\""
        communicator!!.writeUSB(serialString)

        cl = CommandList(instrumentMaster.evNoteOff)
        cmd = cl.buildCommandString(arrayOf<String>("mfm","bmr"))

        if (findViewById<CheckBox>(R.id.checkboxMIDINoteOffSendFullMute).isChecked)
            cmd += ",mfm:1"
        if (findViewById<CheckBox>(R.id.checkboxMIDINoteOffSendFullMute).isChecked)
            cmd += ",bmr:0"
        serialString = "mev:noteoff:\"$cmd\""
        communicator!!.writeUSB(serialString)

        for (i in instrumentMaster.evCC) {
            serialString = "mev:cc:" + i.control + ":'" + i.command + "'"
            communicator!!.writeUSB(serialString)
        }

        var target = getContinuousMapAssociation(findViewById<Spinner>(R.id.spinnerMIDIPitchBendTarget).selectedItem.toString())
        var value = (findViewById<SeekBar>(R.id.seekbarMIDIPitchBendScale).progress / 127).toString()
        if (target == "")
            cmd = "''"
        else
            cmd = "'m:" + currentStringModule.toString() + "," + target + ":(pitch*" + value + ")'"
        serialString = "mev:pb:" + cmd
        communicator!!.writeUSB(serialString)

        target = getContinuousMapAssociation(findViewById<Spinner>(R.id.spinnerMIDIPolyAftertouchTarget).selectedItem.toString())
        value = (findViewById<SeekBar>(R.id.seekbarMIDIPolyAftertouchScale).progress).toString()
        if (target == "")
            cmd = "''"
        else
            cmd = "'m:" + currentStringModule.toString() + "," + target + ":(pressure*" + value + ")'"
        serialString = "mev:pat:" + cmd
        communicator!!.writeUSB(serialString)

        target = getContinuousMapAssociation(findViewById<Spinner>(R.id.spinnerMIDIChannelAftertouchTarget).selectedItem.toString())
        value = (findViewById<SeekBar>(R.id.seekbarMIDIChannelAftertouchScale).progress).toString()
        if (target == "")
            cmd = "''"
        else
            cmd = "'m:" + currentStringModule.toString() + "," + target + ":(pressure*" + value + ")'"
        serialString = "mev:cat:" + cmd
        communicator!!.writeUSB(serialString)

        val targets = getBooleanMapAssociation(findViewById<Spinner>(R.id.spinnerMIDISustainTarget).selectedItem.toString())
        val boolvalue = findViewById<CheckBox>(R.id.checkboxMIDISustainInvert).isChecked
        if (targets == null)
            cmd = "''"
        else {
            cmd = "'m:" + currentStringModule.toString()
            var logic = "bool"
            if (boolvalue) logic = "ibool"
            for (i in targets) {
                cmd += ":" + i.toString() + ":" + logic + "(value)"
            }
            cmd += "'"
        }
        serialString = "mev:cc:64:" + cmd
        communicator!!.writeUSB(serialString)
    }
}

