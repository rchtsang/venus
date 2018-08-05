package venus.riscv.insts.integer.extensions.atomic.r

import venus.riscv.insts.dsl.AMORTypeInstruction

val amominuw = AMORTypeInstruction(
        name = "amominu.w",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)