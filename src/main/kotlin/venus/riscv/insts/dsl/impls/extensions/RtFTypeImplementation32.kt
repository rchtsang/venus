package venus.riscv.insts.dsl.impls.extensions

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.impls.InstructionImplementation
import venus.riscv.insts.floating.Decimal
import venus.simulator.Simulator

class RtFTypeImplementation32(private val eval: (Int) -> Decimal) : InstructionImplementation {
    override operator fun invoke(mcode: MachineCode, sim: Simulator) {
        val rs1 = mcode[InstructionField.RS1]
        val rd = mcode[InstructionField.RD]
        val vrs1 = sim.getReg(rs1)
        sim.setFReg(rd, eval(vrs1))
        sim.incrementPC(mcode.length)
    }
}