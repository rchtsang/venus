package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.Terminal
import venus.vfs.VFSFile

var cp = Command(
        name = "cp",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return Command["cp"].help
            }
            var result = ""
            var f = t.vfs.getObjectFromPath(args[0]) ?: return "cp: could not find the source file!"
            var d = t.vfs.getObjectFromPath(args[1]) ?: return "cp: could not find the destination folder!"
            if (f is VFSFile) {
                val text = f.readText()
                val new_f = VFSFile(f.label, d)
                new_f.setText(text)
                new_f.permissions = f.permissions
                d.addChild(new_f)
            } else {
                result = "cp: Copy currently only works on files!"
            }
            return result
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            return arrayListOf("", ArrayList<String>())
        },
        help = """Creates a text/data file.
            |Usage: touch [filename]
            |NOTE: Does not modify the timestamp at the moment because that is not implemented yet in the VFS.
        """.trimMargin()
)