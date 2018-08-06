package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FFRRTypeInstruction

/*Single-Precision*/
val fles = FFRRTypeInstruction(
        name = "fle.s",
        opcode = 0b1010011,
        funct3 = 0b001,
        funct7 = 0b1010000,
        eval32 = { a, b -> if (a <= b) 1 else 0 }
)