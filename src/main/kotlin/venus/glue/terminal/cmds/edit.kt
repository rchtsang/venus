package venus.glue.terminal.cmds

import venus.glue.Driver
import venus.glue.terminal.Command
import venus.glue.terminal.Terminal
import venus.glue.vfs.VFSFile
import venus.glue.vfs.VFSType

var edit = Command(
        name = "edit",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size != 1) {
                return "edit: Takes in one argument [filename]"
            }
            val obj = t.vfs.getObjectFromPath(args[0])
            if (obj === null) {
                return "edit: '${args[0]}' could not be found!"
            }
            if (obj.type !== VFSType.File) {
                return "edit: Only files can be loaded into the editor."
            }
            try {
                val txt: String = (obj as VFSFile).readText()
                js("codeMirror.setValue(txt);")
                Driver.openEditor()
                js("codeMirror.refresh();")
            } catch (e: Throwable) {
                return "edit: Could not load file to the editor!"
            }
            return ""
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            throw NotImplementedError()
            return ArrayList<String>()
        },
        help = "edit: Takes in one argument [filename] and will copy the contents to the editor tab and then go to the editor tab." +
                "\nUsage: edit [filename]"
)