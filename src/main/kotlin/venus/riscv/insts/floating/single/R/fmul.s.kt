package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSRTypeInstruction

/*Single-Precision*/
val fmuls = FSRTypeInstruction(
        name = "fmul.s",
        opcode = 0b1010011,
        funct7 = 0b0001000,
        eval32 = { a, b -> a * b }
)