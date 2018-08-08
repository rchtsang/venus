package venus.riscv.insts.dsl.impls.extensions

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.impls.InstructionImplementation
import venus.riscv.insts.dsl.impls.signExtend
import venus.riscv.insts.floating.Decimal
import venus.simulator.Simulator

/**
 * Created by thaum on 8/6/2018.
 */
class FITypeImplementation32(private val eval: (Int, Simulator) -> Decimal) : InstructionImplementation {
    override operator fun invoke(mcode: MachineCode, sim: Simulator) {
        val rs1 = mcode[InstructionField.RS1]
        val rd = mcode[InstructionField.RD]
        val vrs1 = sim.getReg(rs1)
        val imm = signExtend(mcode[InstructionField.IMM_11_0], 12)
        sim.setFReg(rd, eval(vrs1 + imm, sim))
        sim.incrementPC(mcode.length)
    }
}