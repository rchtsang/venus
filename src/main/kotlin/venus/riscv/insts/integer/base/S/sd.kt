package venus.riscv.insts.integer.base.s

import venus.riscv.insts.InstructionNotSupportedError
import venus.riscv.insts.dsl.STypeInstruction

val sd = STypeInstruction(
        name = "sw",
        opcode = 0b0100011,
        funct3 = 0b010,
//        store16 = Simulator::storeWordwCache,
        store32 = {sim, a, b ->
            throw InstructionNotSupportedError("SD is not supported by 32 bit systems!")
        }
//        store64 = Simulator::storeWordwCache,
//        store128 = Simulator::storeWordwCache
)
