package venus.riscv.insts.integer.base

import venus.riscv.insts.dsl.LoadTypeInstruction
import venus.simulator.Simulator

val lbu = LoadTypeInstruction(
        name = "lbu",
        opcode = 0b0000011,
        funct3 = 0b100,
//        load16 = NoImplementation,
        load32 = Simulator::loadBytewCache
//        load64 = NoImplementation,
//        load128 = NoImplementation,
)
