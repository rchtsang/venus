package venus.glue.js.terminal

class CommandNotFoundError : Throwable {
    /**
     * @param msg the message to error with
     */

    constructor(command: String? = null) : super(command + ": command not found")
}