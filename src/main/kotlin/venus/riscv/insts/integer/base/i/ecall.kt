package venus.riscv.insts.integer.base.i

import venus.glue.Renderer
import venus.riscv.InstructionField
import venus.riscv.MemorySegments
import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.RawDisassembler
import venus.riscv.insts.dsl.formats.FieldEqual
import venus.riscv.insts.dsl.formats.InstructionFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.RawImplementation
import venus.riscv.insts.dsl.parsers.DoNothingParser
import venus.simulator.Simulator

val ecall = Instruction(
        name = "ecall",
        format = InstructionFormat(4,
                listOf(FieldEqual(InstructionField.ENTIRE, 0b000000000000_00000_000_00000_1110011))
        ),
        parser = DoNothingParser,
        impl16 = NoImplementation,
        impl32 = RawImplementation { mcode, sim ->
            val whichCall = sim.getReg(10)
            when (whichCall) {
                1 -> printInteger(sim)
                4 -> printString(sim)
                9 -> sbrk(sim)
                10 -> exit(sim)
                11 -> printChar(sim)
                13 -> openFile(sim)
                14 -> readFile(sim)
                15 -> writeFile(sim)
                16 -> closeFile(sim)
                17 -> exitWithCode(sim)
                18 -> fflush(sim)
                19 -> feof(sim)
                20 -> ferror(sim)
                34 -> printHex(sim)
                else -> Renderer.printConsole("Invalid ecall $whichCall")
            }
            if (!(whichCall == 10 || whichCall == 17)) {
                sim.incrementPC(mcode.length)
            }
        },
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = RawDisassembler { "ecall" }
)

private fun openFile(sim: Simulator) {
    /**
     * Attempt to open the file with the lowest number to return first. If cannot open file, return -1.
     * Look here for the permissionbits:https://en.cppreference.com/w/c/io/fopen
     *
     * a0=13,a1=filename,a2=permissionbits -> a0=filedescriptor
     */
    // WIP
}

private fun readFile(sim: Simulator) {
    /**
     * Check file descriptor and check if we have the valid permissions.
     * If we can read from the file, start reading at the offset (default=0)
     * and increment the offset by the bytes read. Return the number of bytes which were read.
     * User will call feof(fd) or ferror(fd) for if the output is not equal to the length.
     *
     * a0=14, a1=filedescriptor, a2=where to store data, a3=amt to read -> a0=Number of items read
     */
    // WIP
}

private fun writeFile(sim: Simulator) {
    /**
     * a0=15, a1=filedescriptor, a2=buffer to read data, a3=amt to write, a4=size of each item -> a0=Number of items written
     */
    // WIP
}

private fun closeFile(sim: Simulator) {
    /**
     * Flush the data written to the file back to where it came from.
     * a0=16, a1=filedescriptor -> ​0​ on success, EOF otherwise
     */
    // WIP
}

private fun fflush(sim: Simulator) {
    /**
     * Returns zero on success. Otherwise EOF is returned and the error indicator of the file stream is set.
     * a0=19, a1=filedescriptor -> a0=if end of file
     */
}

private fun feof(sim: Simulator) {
    /**
     * Will return nonzero value if the end of the stream has been reached, otherwise ​0​
     *
     * a0=19, a1=filedescriptor -> a0=if end of file
     */
}

private fun ferror(sim: Simulator) {
    /**
     * Will return Nonzero value if the file stream has errors occurred, ​0​ otherwise
     *
     * a0=20, a1=filedescriptor -> a0=if error occured
     */
}

private fun printHex(sim: Simulator) {
    val arg = sim.getReg(11)
    sim.ecallMsg = Renderer.toHex(arg)
    Renderer.printConsole(sim.ecallMsg)
}

private fun printInteger(sim: Simulator) {
    val arg = sim.getReg(11)
    sim.ecallMsg = arg.toString()
    Renderer.printConsole(sim.ecallMsg)
}

private fun printString(sim: Simulator) {
    var arg = sim.getReg(11)
    var c = sim.loadByte(arg)
    arg++
    while (c != 0) {
        sim.ecallMsg += c.toChar()
        Renderer.printConsole(c.toChar())
        c = sim.loadByte(arg)
        arg++
    }
}

private fun sbrk(sim: Simulator) {
    val bytes = sim.getReg(11)
    if (bytes < 0) return
    sim.setReg(10, sim.getHeapEnd())
    sim.addHeapSpace(bytes)
}

private fun exit(sim: Simulator) {
    sim.setPC(MemorySegments.STATIC_BEGIN)
    // sim.ecallMsg = "exiting the simulator"
}

private fun printChar(sim: Simulator) {
    val arg = sim.getReg(11)
    sim.ecallMsg = (arg.toChar()).toString()
    Renderer.printConsole(arg.toChar())
}

private fun exitWithCode(sim: Simulator) {
    sim.setPC(MemorySegments.STATIC_BEGIN)
    val retVal = sim.getReg(11)
    sim.ecallMsg = "\nExited with error code $retVal"
    Renderer.printConsole("\nExited with error code $retVal\n")
}
