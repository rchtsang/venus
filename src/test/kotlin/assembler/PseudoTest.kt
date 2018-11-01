package venus.assembler

import org.junit.Test
import venus.glue.vfs.VirtualFileSystem
import kotlin.test.assertEquals
import venus.simulator.Simulator
import venus.linker.Linker
import venus.linker.ProgramAndLibraries

class PseudoTest {
    @Test
    fun moveTest() {
        val (prog, _) = Assembler.assemble("""
        addi x1 x0 5
        mv x2 x1
        """)
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertEquals(5, sim.getReg(2))
    }

    @Test fun liTest() {
        val (prog, _) = Assembler.assemble("""
        li x8 2000000000
        li x9 1001
        li x10 3000000005
        li x11 -1234
        """)
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertEquals(2000000000, sim.getReg(8))
        assertEquals(1001, sim.getReg(9))
        assertEquals(-1294967291, sim.getReg(10))
        assertEquals(-1234, sim.getReg(11))
    }
}
