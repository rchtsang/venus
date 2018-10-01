package venus.glue.terminal.cmds

import venus.glue.terminal.Command
import venus.glue.terminal.Terminal
import venus.glue.vfs.VFSLinkedProgram
import venus.glue.vfs.VFSType
import venus.simulator.Simulator

var run = Command(
        name = "run",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size == 0) {
                return "run: Takes in names of programs which you want to link together and run."
            }
            val obj = t.vfs.getObjectFromPath(args[0])
            if (obj === null) {
                return "run: Could not find file ${args[0]}"
            }
            if (obj.type !== VFSType.LinkedProgram) {
                return "run: ${args[0]} must be a linked program! (Use link one or more programs together)"
            }
            args.removeAt(0)
            val linkedProgram = (obj as VFSLinkedProgram).getLinkedProgram()
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
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<String> {
            return ArrayList<String>()
        }
)