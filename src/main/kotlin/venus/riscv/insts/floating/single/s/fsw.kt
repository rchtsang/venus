package venus.riscv.insts.floating.single.s

import venus.riscv.insts.dsl.STypeInstruction

/*Single-Precision*/

val fsw = STypeInstruction(
        name = "fsw",
        opcode = 0b0100111,
        funct3 = 0b010,
//        store16 = NoImplementation,
        store32 = { sim, address, value ->
            sim.storeWordwCache(address, value.toFloat().toRawBits())
        }
//        store64 = NoImplementation,
//        store128 = NoImplementation
)