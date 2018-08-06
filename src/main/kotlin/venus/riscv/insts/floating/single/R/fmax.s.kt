package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FS3RTypeInstruction

/*Single-Precision*/
val fmaxs = FS3RTypeInstruction(
        name = "fmax.s",
        opcode = 0b1010011,
        funct3 = 0b001,
        funct7 = 0b0010100,
        eval32 = { a, b -> maxOf(a, b) }
)