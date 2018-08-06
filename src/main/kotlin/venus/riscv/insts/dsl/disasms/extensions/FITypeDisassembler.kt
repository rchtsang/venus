package venus.riscv.insts.dsl.disasms.extensions

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.InstructionDisassembler
import venus.riscv.insts.dsl.impls.signExtend

/**
 * Created by thaum on 8/6/2018.
 */
object FITypeDisassembler : InstructionDisassembler {
    override fun invoke(mcode: MachineCode): String {
        val name = Instruction[mcode].name
        val rd = mcode[InstructionField.RD]
        val rs1 = mcode[InstructionField.RS1]
        val imm = signExtend(mcode[InstructionField.IMM_11_0], 12)
        return "$name f$rd $imm(x$rs1)"
    }
}