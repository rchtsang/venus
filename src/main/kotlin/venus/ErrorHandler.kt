package venus

/**
 * Created by thaum on 7/27/2018.
 */

internal fun handleError(where: String, error: Throwable, handled: Boolean = false) {
    try {
        if (handled) {
            Renderer.printConsole("[ERROR] An error has occurred!\n\n")
        } else {
            Renderer.printConsole("[ERROR] An uncaught error has occurred! Here are the details that may help solve this issue.\n\n")
        }
        Renderer.printConsole("Error:\n`" + error.toString())
        Renderer.printConsole("\n\nID:\n'" + where + "'!\n")
    } catch (t: Throwable) {
        Renderer.printConsole("An error occurred when trying to handle the error! Please tell me what you did since I do not fully know how you caused this error and could not generate a trace for me to figure that out. All I know is that the error was here: '" + where + "' and was:\n" + error.toString())
    }
}