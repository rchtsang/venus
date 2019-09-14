package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.Terminal

var zip = Command(
        name = "zip",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            throw NotImplementedError()
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            throw NotImplementedError()
        },
        help = "This command prints out the contents of the current folder." +
                "\nIt currently does not take in any arguments." +
                "\nUsage: ls"
)