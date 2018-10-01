package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var mkdir = Command(
        name = "mkdir",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return "mkdir: mkdir takes in a folder name."
            }
            return t.vfs.mkdir(args.joinToString(" "))
        }
)