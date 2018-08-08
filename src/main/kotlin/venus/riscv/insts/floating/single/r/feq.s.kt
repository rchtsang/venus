package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FFRRTypeInstruction

/*Single-Precision*/
val feqs = FFRRTypeInstruction(
        name = "feq.s",
        opcode = 0b1010011,
        funct3 = 0b010,
        funct7 = 0b1010000,
        eval32 = { a, b -> if (a.getFloat() == b.getFloat()) 1 else 0 }
)