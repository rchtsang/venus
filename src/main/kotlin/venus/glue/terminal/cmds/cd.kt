package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var cd = Command(
        name = "cd",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            return t.vfs.cd(args.joinToString(" "))
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = "cd takes in one argument (a path) and goes to the directory." +
                "\nUsage: cd [path]"
)