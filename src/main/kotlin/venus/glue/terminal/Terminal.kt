package venus.glue.terminal

import venus.glue.vfs.VirtualFileSystem

class Terminal(var vfs: VirtualFileSystem) {

    @JsName("processInput") fun processInput(input: String): String {
        try {
            val args = input.trim().split(" ") as MutableList
            try {
                val cmd = Command[args.removeAt(0)]
                return cmd.execute(args, this)
            } catch (e: CommandNotFoundError) {
                return e.message ?: ": command not found"
            }
        } catch (e: Throwable) {
            return "Unknown error occurred!"
        }
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