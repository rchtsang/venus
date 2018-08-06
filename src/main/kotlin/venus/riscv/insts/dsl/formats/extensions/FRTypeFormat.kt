package venus.riscv.insts.dsl.formats.extensions

import venus.riscv.InstructionField
import venus.riscv.insts.dsl.formats.FieldEqual
import venus.riscv.insts.dsl.formats.InstructionFormat

/**
 * Created by thaum on 8/6/2018.
 */
class FRTypeFormat(opcode: Int, funct7: Int) : InstructionFormat(4, listOf(
        FieldEqual(InstructionField.OPCODE, opcode),
        FieldEqual(InstructionField.FUNCT7, funct7)
))
