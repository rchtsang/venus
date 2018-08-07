package venus.riscv.insts.integer.base.i

import venus.riscv.insts.dsl.ITypeInstruction
import venus.riscv.insts.dsl.compareUnsigned
import venus.riscv.insts.dsl.compareUnsignedLong
import venus.riscv.insts.dsl.compareUnsignedShort

val sltiu = ITypeInstruction(
        name = "sltiu",
        opcode = 0b0010011,
        funct3 = 0b011,
        eval16 = { a, b -> if (compareUnsignedShort(a, b) < 0) 1 else 0 },
        eval32 = { a, b -> if (compareUnsigned(a, b) < 0) 1 else 0 },
        eval64 = { a, b -> if (compareUnsignedLong(a, b) < 0) 1 else 0 },
        eval128 = { a, b -> if (compareUnsignedLong(a, b) < 0) 1 else 0 }
)
