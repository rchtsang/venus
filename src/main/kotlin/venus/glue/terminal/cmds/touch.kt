package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var touch = Command(
        name = "touch",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.touch(args.joinToString(" "))
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = """Creates a text/data file.
            |Usage: touch [filename]
            |NOTE: Does not modify the timestamp at the moment because that is not implemented yet in the VFS.
        """.trimMargin()
)