class CC {
    var control:Int = 0
    var command:String = ""

    constructor() {

    }

    constructor(inControl : Int, inCommand:String) {
        control = inControl
        command = inCommand
    }
}

class InstrumentMaster {
    var evNoteOn:String = ""
    var evNoteOff:String = ""
    var evPolyAftertouch:String = ""
    var evProgramChange:String = ""
    var evChannelAftertouch:String = ""
    var evPitchbend:String = ""

    var evCC = mutableListOf<CC>()

    var cmdNoteOn = CommandList()
    var cmdNoteOff = CommandList()
    var cmdPolyAftertouch = CommandList()
    var cmdPitchbend = CommandList()
    var cmdProgramChange = CommandList()
    var cmdChannelAftertouch = CommandList()

    var midiConfigurations = -1
    var midiConfigurationNames = mutableListOf<String>()
    var midiChannel = 0

    fun getCC(control:Int):String? {
        for (i in evCC) {
            if (i.control == control) {
                return i.command
            }
        }
        return null
    }

    fun addCC(control:Int, command:String) {
        for (i in evCC) {
            if (i.control == control) {
                i.command = command
                return
            }
        }
        evCC.add(CC(control, command))
    }

    fun removeCC(control:Int) {
        for (i in evCC)
            if (i.control == control) {
                evCC.remove(i)
                return
            }
    }
}