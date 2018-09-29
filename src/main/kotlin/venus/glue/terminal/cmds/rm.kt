package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var rm = Command(
        name = "rm",
        execute = fun (args: MutableList<String>, t: Terminal): String {
            return t.vfs.remove(args.joinToString(" "))
        }
)