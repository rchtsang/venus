package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FRRTypeInstruction
import venus.riscv.insts.floating.Decimal
import kotlin.math.sqrt

/*Single-Precision*/
val fsqrts = FRRTypeInstruction(
        name = "fsqrt.s",
        opcode = 0b1010011,
        funct7 = 0b0101100,
        rs2 = 0b00000,
        eval32 = { a, b -> Decimal(f = sqrt((a.getFloat()).toDouble()).toFloat()) }
)