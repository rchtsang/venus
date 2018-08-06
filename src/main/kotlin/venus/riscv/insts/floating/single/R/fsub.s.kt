package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSRTypeInstruction

/*Single-Precision*/
val fsubs = FSRTypeInstruction(
        name = "fsub.s",
        opcode = 0b1010011,
        funct7 = 0b0000100,
        eval32 = { a, b -> a - b }
)