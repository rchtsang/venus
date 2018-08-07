package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSR4TypeInstruction

val fnadds = FSR4TypeInstruction(
        name = "fnmadd.s",
        opcode = 0b1001111,
        funct2 = 0b00,
        eval32 = { a, b, c -> -((a * b) + c) }
)