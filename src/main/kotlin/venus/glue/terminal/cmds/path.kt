package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var path = Command(
        name = "path",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.path()
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = """This command prints out the path of the current directory.
            |It does not take any arguments.
            |Usage: path
        """.trimMargin()
)