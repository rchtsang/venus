package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSRTypeInstruction

/*Single-Precision*/
val fdivs = FSRTypeInstruction(
        name = "fdiv.s",
        opcode = 0b1010011,
        funct7 = 0b0001100,
        eval32 = { a, b -> a / b }
)