package venus.simulator

import org.junit.Test
import kotlin.test.assertEquals
import venus.assembler.Assembler
import venus.glue.vfs.VirtualFileSystem
import venus.linker.Linker
import venus.linker.ProgramAndLibraries

class ECALLTest {
    @Test
    fun terminateEarly() {
        val (prog, _) = Assembler.assemble("""
        addi a0 x0 10
        ecall
        addi a0 x0 5""")
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertEquals(10, sim.getReg(10))
    }

    @Test fun malloc() {
        val (prog, _) = Assembler.assemble("""
        # malloc 4 bytes of memory
        addi a0 x0 9
        addi a1 x0 4
        ecall
        mv s0 a0
        # store 5 there
        addi s1 x0 5
        sw s1 0(s0)
        # malloc 4 more bytes
        addi a0 x0 9
        addi a1 x0 4
        ecall
        addi s2 x0 6
        # store 6 there
        sw s2 0(a0)
        # retrieve the old 5
        lw s1 0(s0)""")
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertEquals(5, sim.getReg(9))
    }
}
