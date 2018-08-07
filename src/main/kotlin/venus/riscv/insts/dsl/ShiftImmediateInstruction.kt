package venus.riscv.insts.dsl

import venus.riscv.insts.dsl.disasms.base.ShiftImmediateDisassembler
import venus.riscv.insts.dsl.formats.base.RTypeFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.base.ShiftImmediateImplementation32
import venus.riscv.insts.dsl.parsers.base.ShiftImmediateParser

class ShiftImmediateInstruction(
    name: String,
    funct3: Int,
    funct7: Int,
    eval16: (Short, Short) -> Short = { _, _ -> throw NotImplementedError("no rv16") },
    eval32: (Int, Int) -> Int,
    eval64: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv64") },
    eval128: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv128") }
) : Instruction(
        name = name,
        format = RTypeFormat(
                opcode = 0b0010011,
                funct3 = funct3,
                funct7 = funct7
        ),
        parser = ShiftImmediateParser,
        impl16 = NoImplementation,
        impl32 = ShiftImmediateImplementation32(eval32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = ShiftImmediateDisassembler
)
