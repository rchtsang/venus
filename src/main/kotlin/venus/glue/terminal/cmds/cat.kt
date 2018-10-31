package venus.glue.js.terminal.cmds

import venus.glue.js.terminal.Command
import venus.glue.js.terminal.Terminal

var cat = Command(
        name = "cat",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.cat(args.joinToString(" "))
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size == 1) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = "cat: takes in one argument (a file or path to a file) and prints out the contents of the file." +
                "\nEx cat foo.txt" +
                "\nUsage: cat [path to file]"
)