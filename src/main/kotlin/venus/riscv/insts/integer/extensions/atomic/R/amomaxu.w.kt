package venus.riscv.insts.integer.extensions.atomic.r

import venus.riscv.insts.dsl.AMORTypeInstruction

val amomaxuw = AMORTypeInstruction(
        name = "amomaxu.w",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11100,
        //eval16 = { a, b -> (a + b).toShort() },
        /*todo test if this 'unsigned' conversion works*/
        eval32 = { data, vrs2 -> maxOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)