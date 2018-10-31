package venus.riscv.insts.floating.double.r

import venus.riscv.insts.dsl.floating.FR4TypeInstruction
import venus.riscv.insts.floating.Decimal

val fnmsubd = FR4TypeInstruction(
        name = "fnmsub.d",
        opcode = 0b1001011,
        funct2 = 0b01,
        eval32 = { a, b, c -> Decimal(d = -((a.getCurrentDouble() * b.getCurrentDouble()) - c.getCurrentDouble()), isF = false) }
)