package venus.glue.terminal.cmds

import venus.assembler.Assembler
import venus.glue.terminal.Command
import venus.glue.terminal.Terminal
import venus.glue.vfs.VFSFile
import venus.glue.vfs.VFSLinkedProgram
import venus.glue.vfs.VFSProgram
import venus.glue.vfs.VFSType
import venus.linker.LinkedProgram
import venus.linker.Linker
import venus.riscv.Program
import venus.simulator.Simulator

var run = Command(
        name = "run",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size == 0) {
                return "run: Takes in names of programs which you want to link together and run."
            }
            val programout = "debugger"
            val fs = args
            val linkedprogs = ArrayList<LinkedProgram>()
            val progs = ArrayList<Program>()
            val files = ArrayList<VFSFile>()
            for (file in fs) {
                val obj = t.vfs.getObjectFromPath(file)
                if (obj == null) {
                    return "run: Could not find file $file"
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
                                return "run: You must either have no linked programs or just one linked program!"
                            }
                            linkedprogs.add((obj as VFSLinkedProgram).getLinkedProgram())
                        }
                        else -> { return "run: Unsupported type: ${obj.type}" }
                    }
                }
            }
            if (files.size + progs.size + linkedprogs.size == 0) {
                return "run: Could not find any of the inputted files!"
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
                    return "run: An error occurred when running the linked program: $e"
                }
                linkedprogs.add(linkedProgram)
            }

            // Getting the LinkedProgram which we want to simulate
            if (linkedprogs.size != 1) {
                return msg + "run: There must only be one linked program!"
            }
            val linkedProgram = linkedprogs[0]
            val sim: Simulator
            try {
                sim = Simulator(linkedProgram, t.vfs.simSettings)
                for (arg in args) {
                    sim.addArg(arg)
                }
                sim.run()
            } catch (e: Throwable) {
                return "run: An error occurred when running the programs execution: $e"
            }
            return sim.stdout
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            if (args.size == 1) {
                val prefix = args[args.size - 1]
                return arrayListOf(prefix, t.vfs.filesFromPrefix(prefix))
            }
            return arrayListOf("", ArrayList<String>())
        },
        help = """Runs the inputed linked program with given arguments.
            |Usage: run [program name] [argument]...
        """.trimMargin()
)