package venus.riscv.insts.floating.double.r

import venus.riscv.insts.dsl.floating.FR4TypeInstruction
import venus.riscv.insts.floating.Decimal

val fnaddd = FR4TypeInstruction(
        name = "fnmadd.d",
        opcode = 0b1001111,
        funct2 = 0b01,
        eval32 = { a, b, c -> Decimal(d = -((a.getDouble() * b.getDouble()) + c.getDouble()), isF = false) }
)