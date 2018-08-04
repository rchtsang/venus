package venus.riscv.insts.dsl

import venus.riscv.insts.dsl.disasms.ITypeDisassembler
import venus.riscv.insts.dsl.formats.ITypeFormat
import venus.riscv.insts.dsl.impls.ITypeImplementation32
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.parsers.ITypeParser

class ITypeInstruction(
        name: String,
        opcode: Int,
        funct3: Int,
        eval16: (Short, Short) -> Short = { _, _ -> throw NotImplementedError("no rv16") },
        eval32: (Int, Int) -> Int,
        eval64: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv64") },
        eval128: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv128") }
) : Instruction(
        name = name,
        format = ITypeFormat(opcode, funct3),
        parser = ITypeParser,
        impl16 = NoImplementation,
        impl32 = ITypeImplementation32(eval32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = ITypeDisassembler
)
