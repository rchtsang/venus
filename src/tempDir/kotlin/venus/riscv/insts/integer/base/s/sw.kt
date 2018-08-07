package venus.riscv.insts.integer.base.s

import venus.riscv.insts.dsl.STypeInstruction
import venus.simulator.Simulator

val sw = STypeInstruction(
        name = "sw",
        opcode = 0b0100011,
        funct3 = 0b010,
//        store16 = Simulator::storeWordwCache,
        store32 = Simulator::storeWordwCache
//        store64 = Simulator::storeWordwCache,
//        store128 = Simulator::storeWordwCache
)
