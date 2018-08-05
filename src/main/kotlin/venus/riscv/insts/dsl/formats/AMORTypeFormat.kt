package venus.riscv.insts.dsl.formats

import venus.riscv.InstructionField

class AMORTypeFormat(opcode: Int, funct3: Int, funct5: Int) : InstructionFormat(4, listOf(
        FieldEqual(InstructionField.OPCODE, opcode),
        FieldEqual(InstructionField.FUNCT3, funct3),
        FieldEqual(InstructionField.FUNCT5, funct5)
))
