package venus.riscv.insts.integer.base

import venus.riscv.InstructionField
import venus.riscv.insts.dsl.UTypeInstruction

val lui = UTypeInstruction(
        name = "lui",
        opcode = 0b0110111,
//        impl16 = NoImplementation,
        impl32 = { mcode, sim ->
            val imm = mcode[InstructionField.IMM_31_12] shl 12
            sim.setReg(mcode[InstructionField.RD], imm)
            sim.incrementPC(mcode.length)
        }
//        impl64 = NoImplementation,
//        impl128 = NoImplementation
)
