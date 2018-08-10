package venus.riscv.insts.integer.base.i

import venus.riscv.InstructionField
import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.RawDisassembler
import venus.riscv.insts.dsl.formats.FieldEqual
import venus.riscv.insts.dsl.formats.InstructionFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.parsers.DoNothingParser

val fence = Instruction(
        name = "fence",
        format = InstructionFormat(4,
                listOf(FieldEqual(InstructionField.ENTIRE, 0b000000000000_00000_000_00000_0001111))
        ),
        parser = DoNothingParser,
        impl16 = NoImplementation,
        impl32 = NoImplementation,
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = RawDisassembler { "fence" }
)