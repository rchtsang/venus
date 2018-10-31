package venus.glue.js.terminal.cmds

import venus.glue.js.terminal.Command
import venus.glue.js.terminal.Terminal

var ls = Command(
        name = "ls",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.ls()
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            throw NotImplementedError()
        },
        help = "This command prints out the contents of the current folder." +
                "\nIt currently does not take in any arguments." +
                "\nUsage: ls"
)