package venus.glue.js.terminal.cmds

import venus.glue.js.terminal.Command
import venus.glue.js.terminal.CommandNotImplementedError
import venus.glue.js.terminal.Terminal

var tree = Command(
        name = "tree",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            throw CommandNotImplementedError("tree")
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            throw NotImplementedError()
        }
)