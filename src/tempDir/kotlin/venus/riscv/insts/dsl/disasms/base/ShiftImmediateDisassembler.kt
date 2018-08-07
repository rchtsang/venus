package venus.riscv.insts.dsl.disasms.base

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.InstructionDisassembler

object ShiftImmediateDisassembler : InstructionDisassembler {
    override fun invoke(mcode: MachineCode): String {
        val name = Instruction[mcode].name
        val rd = mcode[InstructionField.RD]
        val rs1 = mcode[InstructionField.RS1]
        val shamt = mcode[InstructionField.SHAMT]
        return "$name x$rd x$rs1 $shamt"
    }
}
