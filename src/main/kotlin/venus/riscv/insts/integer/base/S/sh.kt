package venus.riscv.insts.integer.base.s

import venus.riscv.insts.dsl.STypeInstruction
import venus.simulator.Simulator

val sh = STypeInstruction(
        name = "sh",
        opcode = 0b0100011,
        funct3 = 0b001,
//        store16 = NoImplementation,
        store32 = Simulator::storeHalfWordwCache
//        store64 = NoImplementation,
//        store128 = NoImplementation
)
