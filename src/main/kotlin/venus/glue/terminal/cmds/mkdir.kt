package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var mkdir = Command(
        name = "mkdir",
        execute = fun (args: MutableList<String>, t: Terminal): String {
            return t.vfs.mkdir(args.joinToString(" "))
        }
)