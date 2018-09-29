package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var touch = Command(
        name = "touch",
        execute = fun (args: MutableList<String>, t: Terminal): String {
            return t.vfs.touch(args.joinToString(" "))
        }
)