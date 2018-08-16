package venus.riscv.insts.integer.base.i

import venus.riscv.InstructionField
import venus.riscv.insts.dsl.Instruction
import venus.riscv.insts.dsl.disasms.RawDisassembler
import venus.riscv.insts.dsl.formats.FieldEqual
import venus.riscv.insts.dsl.formats.InstructionFormat
import venus.riscv.insts.dsl.impls.NoImplementation
import venus.riscv.insts.dsl.impls.RawImplementation
import venus.riscv.insts.dsl.parsers.base.CSRTypeParser

val csrrw = Instruction(
        name = "csrrw",
        format = InstructionFormat(4,
                listOf(
                        FieldEqual(InstructionField.OPCODE, 0b1110011),
                        FieldEqual(InstructionField.FUNCT3, 0b001)
                )
        ),
        parser = CSRTypeParser,
        impl16 = NoImplementation,
        impl32 = RawImplementation { mcode, sim ->
            val vrs1 = sim.getReg(mcode[InstructionField.RS1])
            val vcsr = sim.getReg(32)
            sim.setReg(mcode[InstructionField.RD], vcsr)
            sim.setReg(32, vrs1)
            sim.incrementPC(mcode.length)
        },
        impl64 = NoImplementation,
        impl128 = NoImplementation,
        disasm = RawDisassembler {
            val dest = it[InstructionField.RD]
            val source = it[InstructionField.RS1]
            val csr = it[InstructionField.IMM_11_0]
            "csrrw x$dest $csr x$source"
        }
)
