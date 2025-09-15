class StringModule {
    public var name:String = ""
    public val commandValues =  mutableMapOf<String, String>()
    public var adcValue = IntArray(8)
    public var adcCommand = arrayOfNulls<CommandList>(8)
    public var harmonicData = mutableListOf<Float>() //Array<Float> = arrayOf()

}