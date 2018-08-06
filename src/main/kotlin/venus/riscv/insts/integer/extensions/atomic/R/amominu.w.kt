package venus.riscv.insts.integer.extensions.atomic.r

import venus.riscv.insts.dsl.AMORTypeInstruction

val amominuw = AMORTypeInstruction(
        name = "amominu.w",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        rl = 0b0,
        aq = 0b0,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)

val amominuwaq = AMORTypeInstruction(
        name = "amominu.w.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        rl = 0b0,
        aq = 0b1,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)

val amominuwrl = AMORTypeInstruction(
        name = "amominu.w.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        rl = 0b1,
        aq = 0b0,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)

val amominuwaqrl = AMORTypeInstruction(
        name = "amominu.w.aq.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        rl = 0b1,
        aq = 0b1,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)

val amominuwrlaq = AMORTypeInstruction(
        name = "amominu.w.rl.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b11000,
        rl = 0b1,
        aq = 0b1,
        //eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> minOf(data xor Int.MIN_VALUE, vrs2 xor Int.MIN_VALUE) }
        //eval64 = { a, b -> a + b },
        //eval128 = { a, b -> a + b }
)