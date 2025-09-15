import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.truncate

class Helpers {

    class ScaleData(inBase:Int, inRatios:Array<Float>, inNames:Array<String>) {
        var base:Int = 0
        var ratios = arrayOf<Float>()
        var names = arrayOf<String>()

        init {
            base = inBase
            ratios = inRatios
            names = inNames
        }
    }

    var scaleDataJust = ScaleData(440, arrayOf<Float>(1F, 1.06667F, 1.125F, 1.2F, 1.25F, 1.3333F, 1.40625F, 1.5F, 1.6F, 1.66667F, 1.8F, 1.875F),
        arrayOf<String>("A-", "A#", "B-", "C-", "C#", "D-", "D#", "E-", "F-", "F#", "G-", "G#"))

    var scaleDataEqual = ScaleData(440, arrayOf<Float>(1F, 1.059463094F, 1.122462048F, 1.189207115F, 1.259921050F, 1.334839854F, 1.414213562F,
        1.498307077F, 1.587401052F, 1.681792831F, 1.781797436F, 1.887748625F), arrayOf<String>("A-", "A#", "B-", "C-", "C#", "D-", "D#", "E-", "F-",
        "F#", "G-", "G#"))

    init {

    }

    fun getBaseNoteFromFrequency(frequency:Float, scaleData:ScaleData):Array<String> {
        val oct0F = scaleData.base / 32

        var octave = 5
        while(truncate(frequency / (oct0F * (2F).pow(octave) )) < 2) {
            octave -= 1
        }
        octave += 1

        var measurementBaseline = oct0F * (2F).pow(octave)
        var compare:Float = 0F
        var note = 0
        scaleData.ratios.forEach {
            compare = measurementBaseline * it
            if (compare > frequency) return@forEach
            note++
        }
        if (compare <= frequency) note = 11
        note -= 1


        var cents:Float = log2(frequency / (measurementBaseline * scaleData.ratios[note])) * 1200
        if (cents > 60) {
            note += 1
            cents = log2(frequency / (measurementBaseline * scaleData.ratios[note])) * 1200
            if (cents > 60) {
                octave++
                measurementBaseline = oct0F * (2F).pow(octave)
                note = 0
                cents = log2(frequency / (measurementBaseline * scaleData.ratios[note])) * 1200
            }
        }

        val noteName = scaleData.names[note]

        return arrayOf<String>(octave.toString(), note.toString(), cents.toString(), noteName.toString())
    }
    /*

    def getBaseNoteFromFrequency(frequency, noteDataArray):
    oct0f = noteDataArray[0] / 32
    #    print ("frequency " + str(frequency))
    octave = 5
    while math.trunc(frequency / (oct0f * 2 ** octave)) < 2:
    octave -= 1
    octave += 1

    measBaseline = oct0f * 2 ** (octave)

    for note in range(0, len(noteDataArray[1])):
    if measBaseline * noteDataArray[1][note] > frequency: break

    # if we're still under we're just at the break of the next octave
    if measBaseline * noteDataArray[1][note] <= frequency:
    note = 11

    note -= 1

    cents = math.log2(frequency / (measBaseline * noteDataArray[1][note])) * 1200
    #    print("note first try " + str(note))
    #    print("cents first try " + str(cents))
    #  if we are over 60 we should convert to negative by recalculating towards the next note
    if (cents > 60):
    print ("too much, try again")
    note += 1
    cents = math.log2(frequency / (measBaseline * noteDataArray[1][note])) * 1200
    #  if we're still over we're at the break of an octave
    if (cents > 60):
    octave += 1
    measBaseline = oct0f * 2 ** (octave)
    note = 0
    cents = math.log2(frequency / (measBaseline * noteDataArray[1][note])) * 1200

    noteName = noteDataArray[2][note]

    return octave, note, cents, noteName

 */
}