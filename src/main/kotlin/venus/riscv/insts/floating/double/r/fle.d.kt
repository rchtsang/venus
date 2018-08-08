package venus.riscv.insts.floating.double.r

import venus.riscv.insts.dsl.floating.FFRRTypeInstruction

val fled = FFRRTypeInstruction(
        name = "fle.d",
        opcode = 0b1010011,
        funct3 = 0b000,
        funct7 = 0b1010001,
        eval32 = { a, b -> if (a.getDouble() <= b.getDouble()) 1 else 0 }
)