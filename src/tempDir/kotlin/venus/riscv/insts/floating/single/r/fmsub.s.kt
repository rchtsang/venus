package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSR4TypeInstruction

/*Single-Precision*/
val fmsubs = FSR4TypeInstruction(
        name = "fmsub.s",
        opcode = 0b1000111,
        funct2 = 0b00,
        eval32 = { a, b, c -> (a * b) - c }
)