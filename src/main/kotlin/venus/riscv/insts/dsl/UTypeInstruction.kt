package venus.riscv.insts.dsl

import venus.riscv.MachineCode
import venus.riscv.insts.dsl.disasms.base.UTypeDisassembler
import venus.riscv.insts.dsl.formats.base.UTypeFormat
import venus.riscv.insts.dsl.impls.RawImplementation
import venus.riscv.insts.dsl.parsers.base.UTypeParser
import venus.simulator.Simulator

class UTypeInstruction(
    name: String,
    opcode: Int,
    impl16: (MachineCode, Simulator) -> Unit = { _, _ -> throw NotImplementedError("no rv16") },
    impl32: (MachineCode, Simulator) -> Unit,
    impl64: (MachineCode, Simulator) -> Unit = { _, _ -> throw NotImplementedError("no rv64") },
    impl128: (MachineCode, Simulator) -> Unit = { _, _ -> throw NotImplementedError("no rv128") }
) : Instruction(
        name = name,
        format = UTypeFormat(opcode),
        parser = UTypeParser,
        impl16 = RawImplementation(impl16),
        impl32 = RawImplementation(impl32),
        impl64 = RawImplementation(impl64),
        impl128 = RawImplementation(impl128),
        disasm = UTypeDisassembler
)
