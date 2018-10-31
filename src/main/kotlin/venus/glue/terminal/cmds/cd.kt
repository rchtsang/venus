package venus.glue.js.terminal.cmds

import venus.glue.js.terminal.Command
import venus.glue.js.terminal.Terminal

var cd = Command(
        name = "cd",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.cd(args.joinToString(" "))
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size == 1) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = "cd takes in one argument (a path) and goes to the directory." +
                "\nUsage: cd [path]"
)