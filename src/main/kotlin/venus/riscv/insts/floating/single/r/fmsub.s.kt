package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FR4TypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/
val fmsubs = FR4TypeInstruction(
        name = "fmsub.s",
        opcode = 0b1000111,
        funct2 = 0b00,
        eval32 = { a, b, c -> Decimal(f = (a.getCurrentFloat() * b.getCurrentFloat()) - c.getCurrentFloat()) }
)