package venus.glue.terminal.cmds

/* ktlint-disable no-wildcard-imports */
import venus.assembler.Assembler
import venus.glue.Driver
import venus.glue.Renderer
import venus.glue.terminal.Command
import venus.glue.terminal.Terminal
import venus.glue.vfs.*
import venus.linker.LinkedProgram
import venus.linker.Linker
import venus.riscv.Address
import venus.riscv.MemSize
import venus.riscv.Program
/* ktlint-enable no-wildcard-imports */

var gdb = Command(
        name = "gdb",
        // @TODO Fix how will intemperate files vs args.
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return "gdb: Takes in at least one File/Program or a single LinkedProgram"
            }
            val programout = "debugger"
            val fs = args
            val linkedprogs = ArrayList<LinkedProgram>()
            val progs = ArrayList<Program>()
            val files = ArrayList<VFSFile>()
            for (file in fs) {
                val obj = t.vfs.getObjectFromPath(file)
                if (obj == null) {
                    return "gdb: Could not find file $file"
                } else {
                    when (obj.type) {
                        VFSType.File -> {
                            files.add(obj as VFSFile)
                        }
                        VFSType.Program -> {
                            progs.add((obj as VFSProgram).getProgram())
                        }
                        VFSType.LinkedProgram -> {
                            if (files.size + progs.size + linkedprogs.size > 0) {
                                return "gdb: You must either have no linked programs or just one linked program!"
                            }
                            linkedprogs.add((obj as VFSLinkedProgram).getLinkedProgram())
                        }
                        else -> { return "gdb: Unsupported type: ${obj.type}" }
                    }
                }
            }
            if (files.size + progs.size + linkedprogs.size == 0) {
                return "gdb: Could not find any of the inputted files!"
            }

            // Assembly stage for any files
            var msg = ""
            for (file in files) {
                val (prog, errors, warnings) = Assembler.assemble(file.contents.get(VFSFile.innerTxt) as String, file.label)
                if (errors.isNotEmpty()) {
                    msg += "assemble: Could not assemble file! Here are the errors:"
                    for (error in errors) {
                        msg += "\n" + error.toString()
                    }
                    return msg
                }
                if (warnings.isNotEmpty()) {
                    msg += "assemble: Assembled file with a few warnings:"
                    for (warning in warnings) {
                        msg += "\n" + warning.toString()
                    }
                }
                progs.add(prog)
            }

            // Linking Stage for any programs
            if (progs.size > 0) {
                val linkedProgram: LinkedProgram
                try {
                    linkedProgram = Linker.link(progs)
                } catch (e: Throwable) {
                    return "gdb: An error occurred when running the linked program: $e"
                }
                linkedprogs.add(linkedProgram)
            }

            // Getting the LinkedProgram which we want to simulate
            if (linkedprogs.size != 1) {
                return msg + "gdb: There must only be one linked program!"
            }
            val lp = linkedprogs[0]
            try {
                Driver.loadSim(lp)
                Renderer.loadSimulator(Driver.sim)
                Driver.setCacheSettings()
                Renderer.updateCache(Address(0, MemSize.WORD))
                Driver.openSimulator()
            } catch (e: Throwable) {
                console.error(e)
                return "gdb: An error has occurred"
            }
            return ""
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size > 0) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        }
)