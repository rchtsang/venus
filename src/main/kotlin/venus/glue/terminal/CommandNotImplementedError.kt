package venus.glue.js.terminal

class CommandNotImplementedError : Throwable {
    /**
     * @param msg the message to error with
     */

    constructor(command: String? = null) : super(command + ": command found but not implemented yet!")
}