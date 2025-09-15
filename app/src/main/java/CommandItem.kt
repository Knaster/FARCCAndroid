class CommandItem(commandString: String) {
    public var command: String = ""
    //public var argument: MutableMap<String, String> = mutableMapOf()
    public var argument: MutableList<String> = mutableListOf()

    fun parseCommand(commandString: String) {
        var temp = commandString.trim()
        var foundIndex = temp.indexOf(':')

        if (foundIndex == -1) {
            this.command = commandString
            return
        } else {
            this.command = commandString.substring(0,foundIndex)
        }
        foundIndex += 1
        var startIndex = foundIndex
        while (foundIndex < commandString.length) {
            if ((commandString[foundIndex] == ':') && (startIndex != foundIndex)) {
                this.argument.add(commandString.substring(startIndex, foundIndex))
                if (this.argument.count() > 1) {
                    if (this.argument[1] == "bchsh") {
                        val ass = 5
                    }
                }
                startIndex = foundIndex + 1
            } else if ((commandString[foundIndex] == '"') or (commandString[foundIndex] == '\'')) {
                startIndex = foundIndex
                foundIndex = startOfQuote(commandString, foundIndex + 1, commandString[foundIndex])
                this.argument.add(commandString.substring(startIndex + 1, foundIndex))
                foundIndex += 1
                startIndex = foundIndex + 1
            }
            foundIndex += 1
        }
        if (startIndex < foundIndex) {
            this.argument.add(commandString.substring(startIndex, foundIndex))
        }
    }

    fun startOfQuote(commandString: String, index: Int, quote: Char):Int {
        var i = index
        while (i < commandString.length) {
            if (commandString[i] == quote) {
                return i
            }
            i += 1
        }
        return -1
    }


    init {
        parseCommand(commandString)
    }
}