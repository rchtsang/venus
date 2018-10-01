package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var cat = Command(
        name = "cat",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.cat(args.joinToString(" "))
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = "cat: takes in one argument (a file or path to a file) and prints out the contents of the file." +
                "\nEx cat foo.txt" +
                "\nUsage: cat [path to file]"
)