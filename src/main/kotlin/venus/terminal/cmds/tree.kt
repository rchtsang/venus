package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.CommandNotImplementedError
import venus.terminal.Terminal

var tree = Command(
        name = "tree",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            throw CommandNotImplementedError("tree")
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            throw NotImplementedError()
        }
)