package venus.glue.terminal

import venus.glue.vfs.VirtualFileSystem

class Terminal(var vfs: VirtualFileSystem) {

    @JsName("processInput") fun processInput(input: String): String {
        try {
            try {
                val args = this.extractArgs(input)
                var sudo = if (args[0].toLowerCase() === "sudo") {
                    args.removeAt(0)
                    true
                } else {
                    false
                }
                val cmd = Command[args.removeAt(0)]
                return cmd.execute(args, this, sudo)
            } catch (e: CommandNotFoundError) {
                return e.message ?: ": command not found"
            }
        } catch (e: Throwable) {
            console.error(e)
            return "Unknown error occurred: " + e.toString()
        }
    }

    fun extractArgs(input: String): ArrayList<String> {
        return input.split(" ") as ArrayList<String>
    }

    @JsName("getCommands") fun getCommands() {
        js("var cmds = []")
        var ktcmds = Command.getCommands()
        for (c in ktcmds) {
            js("cmds.push(c)")
        }
        return js("cmds")
    }
}