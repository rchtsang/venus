package venus.glue.terminal

import venus.glue.vfs.VirtualFileSystem

class Terminal(var vfs: VirtualFileSystem) {

    @JsName("processInput") fun processInput(input: String): String {
        return input
    }

    @JsName("getCommands") fun getCommands() {
        return js("[]")
    }
}