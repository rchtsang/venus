package venus.riscv.insts.dsl.formats.extensions

import venus.riscv.InstructionField
import venus.riscv.insts.dsl.formats.FieldEqual
import venus.riscv.insts.dsl.formats.InstructionFormat

class FSRS2TypeFormat(opcode: Int, funct7: Int, rs2: Int) : InstructionFormat(4, listOf(
        FieldEqual(InstructionField.OPCODE, opcode),
        FieldEqual(InstructionField.RS2, rs2),
        FieldEqual(InstructionField.FUNCT7, funct7)
))
