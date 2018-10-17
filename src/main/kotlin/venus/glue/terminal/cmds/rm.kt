package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var rm = Command(
        name = "rm",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            var output = ""
            var fails = 0
            var attempts = 0
            for (arg in args) {
                attempts++
                val out = t.vfs.remove(arg)
                if (out != "") {
                    output += out + "\n"
                    fails++
                }
            }
            if (fails < attempts) {
                t.vfs.save()
            }
            return output
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size > 0) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = """Remove (unlink) the FILE(s),
            |Usage: rm [OPTION]... [FILE]...
            |NOTE: Options and multiple file input is currently not implemented yet.
        """.trimMargin()
)