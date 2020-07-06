package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.Command.Companion.fileTabComplete
import venus.terminal.Terminal

var mv = Command(
        name = "mv",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return Command["cp"].help
            }
            var result = ""
            var f = t.vfs.getObjectFromPath(args[0]) ?: return "mv: could not find the source file!"
            var d = t.vfs.getObjectFromPath(args[1]) ?: return "mv: could not find the destination folder!"
            f.parent.removeChild(f.label)
            d.addChild(f)
            f.parent = d
            return result
        },
        tab = ::fileTabComplete,
        help = """Moves a file/folder to a new location.
            |Usage: mv [source] [destination]
            |NOTE: This is a very dumb implementation of move and does not do any fancy things linux move does.
        """.trimMargin()
)