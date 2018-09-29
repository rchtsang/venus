package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var ls = Command(
        name = "ls",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.ls()
        }
)