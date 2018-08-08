package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FRTypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/
val fmuls = FRTypeInstruction(
        name = "fmul.s",
        opcode = 0b1010011,
        funct7 = 0b0001000,
        eval32 = { a, b -> Decimal(f = a.getFloat() * b.getFloat()) }
)