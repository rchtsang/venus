package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSRRTypeInstruction
import kotlin.js.Math

/*Single-Precision*/
val fsqrts = FSRRTypeInstruction(
        name = "fsqrt.s",
        opcode = 0b1010011,
        funct7 = 0b0101100,
        rs2 = 0b00000,
        eval32 = { a, b -> Math.sqrt(a.toDouble()).toFloat() }
)