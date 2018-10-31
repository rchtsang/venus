package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FRTypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/
val fadds = FRTypeInstruction(
        name = "fadd.s",
        opcode = 0b1010011,
        funct7 = 0b0000000,
        eval32 = { a, b -> Decimal(f = a.getCurrentFloat() + b.getCurrentFloat()) }
)