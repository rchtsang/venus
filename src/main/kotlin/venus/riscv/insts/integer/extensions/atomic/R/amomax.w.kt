package venus.riscv.insts.integer.extensions.atomic.r

import venus.riscv.insts.dsl.AMORTypeInstruction

val amomaxw = AMORTypeInstruction(
        name = "amomax.w",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b10100,
        rl = 0b0,
        aq = 0b0,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> maxOf(data, vrs2) }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val amomaxwaq = AMORTypeInstruction(
        name = "amomax.w.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b10100,
        rl = 0b0,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> maxOf(data, vrs2) }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val amomaxwrl = AMORTypeInstruction(
        name = "amomax.w.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b10100,
        rl = 0b1,
        aq = 0b0,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> maxOf(data, vrs2) }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val amomaxwaqrl = AMORTypeInstruction(
        name = "amomax.w.aq.rl",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b10100,
        rl = 0b1,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> maxOf(data, vrs2) }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)

val amomaxwrlaq = AMORTypeInstruction(
        name = "amomax.w.rl.aq",
        opcode = 0b0101111,
        funct3 = 0b010,
        funct5 = 0b10100,
        rl = 0b1,
        aq = 0b1,
        // eval16 = { a, b -> (a + b).toShort() },
        eval32 = { data, vrs2 -> maxOf(data, vrs2) }
        // eval64 = { a, b -> a + b },
        // eval128 = { a, b -> a + b }
)