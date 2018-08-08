package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FRTypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/
val fdivs = FRTypeInstruction(
        name = "fdiv.s",
        opcode = 0b1010011,
        funct7 = 0b0001100,
        eval32 = { a, b -> Decimal(f = a.getFloat() / b.getFloat()) }
)