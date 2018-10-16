package venus.glue.terminal.cmds

import venus.glue.Driver
import venus.glue.Renderer
import venus.glue.terminal.Command
import venus.glue.terminal.CommandNotImplementedError
import venus.glue.terminal.Terminal
import venus.glue.vfs.VFSLinkedProgram
import venus.glue.vfs.VFSType
import venus.riscv.Address
import venus.riscv.MemSize
import venus.simulator.Simulator

var gdb = Command(
        name = "gdb",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            throw CommandNotImplementedError("gdb")
            if (args.size != 1) {
                return "gdb: This currently just takes in one argument [a linked program]"
            }
            val obj = t.vfs.getObjectFromPath(args[0])
            if (obj === null) {
                return "gdb: Could not find file ${args[0]}!"
            }
            if (obj.type !== VFSType.LinkedProgram) {
                return "gdb: You must input a linked program!"
            }

            try {
                val sim = Simulator((obj as VFSLinkedProgram).getLinkedProgram(), t.vfs.simSettings)
                Renderer.renderSimulator(sim)
                Driver.setCacheSettings()
                Renderer.updateCache(Address(0, MemSize.WORD))
            } catch (e: Throwable) {
                console.error(e)
                return "gdb: An error has occurred"
            }
            return ""
        },
        tab = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            throw NotImplementedError()
        }
)