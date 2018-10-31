package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.F3RTypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/
val fmaxs = F3RTypeInstruction(
        name = "fmax.s",
        opcode = 0b1010011,
        funct3 = 0b001,
        funct7 = 0b0010100,
        eval32 = { a, b -> Decimal(f = maxOf(a.getCurrentFloat(), b.getCurrentFloat())) }
)