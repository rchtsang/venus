package venus.riscv.insts.floating.double.r

import venus.riscv.insts.dsl.floating.FRTypeInstruction
import venus.riscv.insts.floating.Decimal

val fmuld = FRTypeInstruction(
        name = "fmul.d",
        opcode = 0b1010011,
        funct7 = 0b0001001,
        eval32 = { a, b -> Decimal(d = a.getDouble() * b.getDouble(), isF = false) }
)