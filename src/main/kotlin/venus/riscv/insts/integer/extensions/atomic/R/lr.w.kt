package venus.riscv.insts.integer.extensions.atomic.r

import venus.riscv.insts.dsl.AMORTypeInstruction
/*@FIXME*/
val lrw = AMORTypeInstruction(
        name = "lr.w",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b00010,
        rl = 0b0,
        aq = 0b0,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> data }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val lrwaq = AMORTypeInstruction(
        name = "lr.w.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b00010,
        rl = 0b0,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> data }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val lrwrl = AMORTypeInstruction(
        name = "lr.w.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b00010,
        rl = 0b1,
        aq = 0b0,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> data }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val lrwaqrl = AMORTypeInstruction(
        name = "lr.w.aq.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b00010,
        rl = 0b1,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> data }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val lrwrlaq = AMORTypeInstruction(
        name = "lr.w.rl.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b00010,
        rl = 0b1,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> data }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)