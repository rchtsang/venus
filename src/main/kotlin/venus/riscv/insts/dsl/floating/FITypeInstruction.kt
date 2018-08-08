package venus.riscv.insts.dsl.floating

import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.extensions.FITypeDisassembler
import venus.riscv.insts.dsl.formats.base.ITypeFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.extensions.FITypeImplementation32
import venus.riscv.insts.dsl.parsers.extensions.FITypeParser
import venus.riscv.insts.floating.Decimal
import venus.simulator.Simulator

class FITypeInstruction(
        name: String,
        opcode: Int,
        funct3: Int,
        eval32: (Int, Simulator) -> Decimal
) : Instruction(
        name = name,
        format = ITypeFormat(opcode, funct3),
        parser = FITypeParser,
        impl16 = NoImplementation,
        impl32 = FITypeImplementation32(eval32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = FITypeDisassembler
)