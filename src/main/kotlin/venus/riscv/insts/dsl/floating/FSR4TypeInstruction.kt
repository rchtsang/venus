package venus.riscv.insts.dsl.floating

import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.extensions.FR4TypeDisassembler
import venus.riscv.insts.dsl.formats.extensions.R4TypeFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.extensions.FR4TypeImplementation32
import venus.riscv.insts.dsl.parsers.extensions.FR4TypeParser
class FSR4TypeInstruction(
    name: String,
    opcode: Int,
    funct2: Int,
        // eval16: (Short, Short) -> Short = { _, _ -> throw NotImplementedError("no rv16") },
    eval32: (Float, Float, Float) -> Float // ,
        // eval64: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv64") },
        // eval128: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv128") }
) : Instruction(
        name = name,
        format = R4TypeFormat(opcode, funct2),
        parser = FR4TypeParser,
        impl16 = NoImplementation,
        impl32 = FR4TypeImplementation32(eval32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = FR4TypeDisassembler
)