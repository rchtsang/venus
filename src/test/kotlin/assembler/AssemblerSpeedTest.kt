package venus.assembler

import org.junit.Test
import venus.glue.vfs.VirtualFileSystem
import kotlin.test.assertTrue
import venus.simulator.Simulator
import venus.linker.Linker
import venus.linker.ProgramAndLibraries

class AssemblerSpeedTest {
    @Test
    fun nopRepeat() {
        val (prog, _) = Assembler.assemble("nop\n".repeat(1000))
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertTrue(true)
    }
}
