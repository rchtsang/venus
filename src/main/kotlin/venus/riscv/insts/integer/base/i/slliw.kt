package venus.riscv.insts.integer.base.i

import venus.riscv.insts.InstructionNotSupportedError
import venus.riscv.insts.dsl.ShiftWImmediateInstruction

val slliw = ShiftWImmediateInstruction(
        name = "slliw",
        opcode = 0b0011011,
        funct3 = 0b001,
        funct7 = 0b0000000,
        eval32 = { a, b ->
            throw InstructionNotSupportedError("addiw is not supported on 32 bit systems!")
        }
)