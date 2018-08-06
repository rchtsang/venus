package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FS3RTypeInstruction

/*Single-Precision*/
val fmins = FS3RTypeInstruction(
        name = "fmin.s",
        opcode = 0b1010011,
        funct3 = 0b000,
        funct7 = 0b0010100,
        eval32 = { a, b -> minOf(a, b) }
)