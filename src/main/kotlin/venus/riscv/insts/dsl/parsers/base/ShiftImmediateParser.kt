package venus.riscv.insts.dsl.parsers.base

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.Program
import venus.riscv.insts.dsl.getImmediate
import venus.riscv.insts.dsl.parsers.InstructionParser
import venus.riscv.insts.dsl.parsers.checkArgsLength
import venus.riscv.insts.dsl.parsers.regNameToNumber

object ShiftImmediateParser : InstructionParser {
    const val SHIFT_MIN = 0
    const val SHIFT_MAX = 31
    override operator fun invoke(prog: Program, mcode: MachineCode, args: List<String>) {
        checkArgsLength(args.size, 3)

        mcode[InstructionField.RD] = regNameToNumber(args[0])
        mcode[InstructionField.RS1] = regNameToNumber(args[1])
        mcode[InstructionField.SHAMT] = getImmediate(args[2], SHIFT_MIN, SHIFT_MAX)
    }
}
