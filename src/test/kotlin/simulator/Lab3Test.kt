/* ktlint-disable package-name */
package venus.simulator
/* ktlint-enable package-name */

import org.junit.Test
import kotlin.test.assertEquals
import venus.assembler.Assembler
import venus.glue.vfs.VirtualFileSystem
import venus.linker.Linker
import venus.linker.ProgramAndLibraries

class Lab3Test {
    @Test
    fun Ex1() {
        val (prog, _) = Assembler.assemble(
"""
        .data
        .word 2, 4, 6, 8
n:      .word 9


        .text
main: 		add     t0, x0, x0
		addi    t1, x0, 1
		la      t3, n
		lw      t3, 0(t3)
fib: 		beq     t3, x0, finish
		add     t2, t1, t0
		mv      t0, t1
		mv      t1, t2
		addi    t3, t3, -1
		jal     x0, fib
finish: addi    a0, x0, 1
		addi    a1, t0, 0
		# ecall
		addi    a0, x0, 10
		ecall
"""
        )
        val PandL = ProgramAndLibraries(listOf(prog), VirtualFileSystem("dummy"))
        val linked = Linker.link(PandL)
        val sim = Simulator(linked)
        sim.run()
        assertEquals(34, sim.getReg(11))
    }
}
