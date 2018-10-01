package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.CommandNotImplementedError
import venus.glue.terminal.Terminal

var tree = Command(
        name = "tree",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            throw CommandNotImplementedError("tree")
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        }
)