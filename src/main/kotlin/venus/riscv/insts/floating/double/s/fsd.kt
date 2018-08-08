package venus.riscv.insts.floating.double.s

import venus.riscv.insts.dsl.STypeInstruction

val fsd = STypeInstruction(
        name = "fsd",
        opcode = 0b0100111,
        funct3 = 0b011,
//        store16 = NoImplementation,
        store32 = { sim, address, value ->
            sim.storeWordwCache(address, (value.toDouble().toRawBits() and 0xFFFFFFFF).toInt())
            sim.storeWordwCache(address + 4, ((value.toDouble().toRawBits() shr 32) and 0xFFFFFFFF).toInt())
        }
//        store64 = NoImplementation,
//        store128 = NoImplementation
)