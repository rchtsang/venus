package venus.glue.terminal.cmds

import venus.glue.Driver
import venus.glue.terminal.Command
import venus.glue.terminal.Terminal

var save = Command(
        name = "save",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size != 1) {
                return "save: Takes in one argument [filename] which you want to save the editor to."
            }
            val txt: String
            try {
                js("codeMirror.save();")
                txt = Driver.getText()
            } catch (e: Throwable) {
                console.error(e)
                return "save: Could not save file!"
            }
            return t.vfs.write(args[0], txt)
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size == 1) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = """Saves the data in the editor to the specified file.
            |Usage: save [filename]
        """.trimMargin()
)