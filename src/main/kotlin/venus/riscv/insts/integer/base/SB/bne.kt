package venus.riscv.insts.integer.base.SB

import venus.riscv.insts.dsl.BTypeInstruction

val bne = BTypeInstruction(
        name = "bne",
        opcode = 0b1100011,
        funct3 = 0b001,
        cond16 = { a, b -> a != b },
        cond32 = { a, b -> a != b },
        cond64 = { a, b -> a != b },
        cond128 = { a, b -> a != b }
)
