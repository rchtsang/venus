package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var rm = Command(
        name = "rm",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.remove(args.joinToString(" "))
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = """Remove (unlink) the FILE(s),
            |Usage: rm [OPTION]... [FILE]...
            |NOTE: Options and multiple file input is currently not implemented yet.
        """.trimMargin()
)