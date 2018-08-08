package venus.riscv.insts.dsl.floating

import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.extensions.FSTypeDisassembler
import venus.riscv.insts.dsl.formats.base.STypeFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.extensions.FSTypeImplementation32
import venus.riscv.insts.dsl.parsers.extensions.FSTypeParser
import venus.riscv.insts.floating.Decimal
import venus.simulator.Simulator

class FSTypeInstruction(
    name: String,
    opcode: Int,
    funct3: Int,
    store32: (Simulator, Int, Decimal) -> Unit
) : Instruction(
        name = name,
        format = STypeFormat(opcode, funct3),
        parser = FSTypeParser,
        impl16 = NoImplementation,
        impl32 = FSTypeImplementation32(store32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = FSTypeDisassembler
)