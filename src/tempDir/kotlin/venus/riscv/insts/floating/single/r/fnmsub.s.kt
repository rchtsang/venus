package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSR4TypeInstruction

val fnsubs = FSR4TypeInstruction(
        name = "fnmsub.s",
        opcode = 0b1001011,
        funct2 = 0b00,
        eval32 = { a, b, c -> -((a * b) - c) }
)