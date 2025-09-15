class CommandList {
    public var commands:MutableList<CommandItem> = mutableListOf()
    public var processingCommands:Boolean = false

    init {
    }
    constructor() {

    }

    constructor(commandString: String) {
        if (commandString != "") {
            addCommands(commandString)
        }
    }

    fun clear() {
        commands.clear()
    }

    /*
        def getCommandAttribute(self, command, attribute):
        for i in self.commands:
            if i.command == command:
                if len(i.argument) > attribute:
                    return i.argument[attribute] #[0]
                else:
                    return ""
        return ""
     */

    fun getCommandAttribute(command:String, attribute:Int):String {
        for (i in commands) {
            if (i.command == command) {
                if (i.argument.count() > attribute) {
                    return i.argument[attribute]
                } else {
                    return ""
                }
            }
        }
        return ""
    }

    fun addCommands(commandString: String) {
        var startIndex = 0
        var foundIndex = 0
        while (foundIndex < commandString.length) {
            if ((commandString[foundIndex] == '"') or (commandString[foundIndex] == '\'')) {
                foundIndex = startOfQuote(commandString, foundIndex + 1, commandString[foundIndex])
                if (foundIndex == -1) {
                    return
                }
            } else if (commandString[foundIndex] == ',') {
                commands.add(CommandItem(commandString.substring(startIndex, foundIndex)))
                startIndex = foundIndex + 1
            }
            foundIndex += 1
        }
        if (startIndex < foundIndex) {
            commands.add(CommandItem(commandString.substring(startIndex, foundIndex)))
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
/*
    def buildCommandString(self, ignoreCommands):
    commandString = ""
    for i in self.commands:
    found = False
    for a in ignoreCommands:
    if i.command == a:
    found = True
    if not found:
    commandString += i.command
    for a in i.argument:
    commandString += ":" + a
    commandString += ","
    return commandString[0:len(commandString) - 1]
*/
    fun buildCommandString(ignoreCommands : Array<String>):String {
        var commandString = ""
        for (i in commands) {
            var found = false
            for (a in ignoreCommands)
                if (i.command == a) found = true
            if (!found) {
                commandString += i.command
                for (a in i.argument) commandString += ":" + a
                commandString += ","
            }
        }
        return commandString
    }
}