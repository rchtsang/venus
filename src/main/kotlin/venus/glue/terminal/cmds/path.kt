package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var path = Command(
        name = "path",
        execute = fun (args: MutableList<String>, t: Terminal): String {
            return t.vfs.path()
        }
)