package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var touch = Command(
        name = "touch",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return Command["touch"].help
            }
            val result = t.vfs.touch(args[0])
            if (result == "") {
                t.vfs.save()
            }
            return result
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            return arrayListOf("", ArrayList<String>())
        },
        help = """Creates a text/data file.
            |Usage: touch [filename]
            |NOTE: Does not modify the timestamp at the moment because that is not implemented yet in the VFS.
        """.trimMargin()
)