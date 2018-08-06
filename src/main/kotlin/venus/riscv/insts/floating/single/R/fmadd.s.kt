package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FSR4TypeInstruction

/*Single-Precision*/
val fmadds = FSR4TypeInstruction(
        name = "fmadd.s",
        opcode = 0b1000011,
        funct2 = 0b00,
        eval32 = { a, b, c -> (a * b) + c }
)