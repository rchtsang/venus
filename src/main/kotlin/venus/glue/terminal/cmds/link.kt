package venus.glue.js.terminal.cmds

import venus.glue.js.terminal.Command
import venus.glue.js.terminal.Terminal
import venus.glue.js.vfs.VFSLinkedProgram
import venus.glue.js.vfs.VFSObject
import venus.glue.js.vfs.VFSProgram
import venus.glue.js.vfs.VFSType
import venus.linker.LinkedProgram
import venus.linker.Linker
import venus.riscv.Program

var link = Command(
        name = "link",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size == 0) {
                return "link: Takes in names of programs which you want to link together. EG link out.l a.out b.out c.out"
            }
            if (args.size == 1) {
                return "link: Takes in a minimum of two args link [output] [input], ..."
            }
            val progs = ArrayList<Program>()
            val output = args.first()
            args.removeAt(0)
            for (program in args) {
                val obj = t.vfs.getObjectFromPath(program)
                if (obj === null) {
                    return "link: Could not find file at path to $program"
                }
                if (obj.type != VFSType.Program) {
                    return "link: The inputs must be programs! ($program)"
                }
                progs.add((obj as VFSProgram).getProgram())
            }
            val linkedProgram: LinkedProgram
            try {
                linkedProgram = Linker.link(progs)
            } catch (e: Throwable) {
                return "link: An error occurred when running the linked program: $e"
            }
            if (!VFSObject.isValidName(output)) {
                return "link: The name of the output file is not valid! ($output)"
            }
            val obj = VFSLinkedProgram(output, t.vfs.currentLocation, linkedProgram)
            return if (t.vfs.currentLocation.addChild(obj)) "" else "link: Could not add linked program to the files!"
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size > 1) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = "This command takes in names of programs which you want to link together." +
                "It only requires one program but takes in an arbitrary number of programs." +
                "\nEG link out.l a.out b.out c.out" +
                "\nUsage: link [output] [input program 1] {[input program 2] ... [input program n]}"
)