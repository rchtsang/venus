package venus.riscv.insts.dsl.floating

import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.extensions.FRTypeDisassembler
import venus.riscv.insts.dsl.formats.extensions.FRTypeFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.extensions.FRTypeImplementation32
import venus.riscv.insts.dsl.parsers.extensions.FRTypeParser

/**
 * Created by thaum on 8/6/2018.
 */
class FSRTypeInstruction(
    name: String,
    opcode: Int,
    funct7: Int,
        // eval16: (Short, Short) -> Short = { _, _ -> throw NotImplementedError("no rv16") },
    eval32: (Float, Float) -> Float // ,
        // eval64: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv64") },
        // eval128: (Long, Long) -> Long = { _, _ -> throw NotImplementedError("no rv128") }
) : Instruction(
        name = name,
        format = FRTypeFormat(opcode, funct7),
        parser = FRTypeParser,
        impl16 = NoImplementation,
        impl32 = FRTypeImplementation32(eval32),
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = FRTypeDisassembler
)