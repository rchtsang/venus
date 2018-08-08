package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.RtFTypeInstruction
import venus.riscv.insts.floating.Decimal

val fcvtsw = RtFTypeInstruction(
    name = "fcvt.s.w",
    opcode = 0b1010011,
    funct7 = 0b1101000,
    funct3 = 0b000,
    rs2 = 0b00000,
    eval32 = { a -> Decimal(f = a.toFloat()) }
)