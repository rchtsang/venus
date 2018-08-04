package venus.riscv.insts.integer.base.SB

import venus.riscv.insts.dsl.BTypeInstruction
import venus.riscv.insts.dsl.compareUnsigned
import venus.riscv.insts.dsl.compareUnsignedLong
import venus.riscv.insts.dsl.compareUnsignedShort

val bltu = BTypeInstruction(
        name = "bltu",
        opcode = 0b1100011,
        funct3 = 0b110,
        cond16 = { a, b -> compareUnsignedShort(a, b) < 0 },
        cond32 = { a, b -> compareUnsigned(a, b) < 0 },
        cond64 = { a, b -> compareUnsignedLong(a, b) < 0 },
        cond128 = { a, b -> compareUnsignedLong(a, b) < 0 }
)
