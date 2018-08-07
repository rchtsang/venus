package venus.riscv.insts.dsl.impls.extensions

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.impls.InstructionImplementation
import venus.simulator.Simulator

/**
 * Created by thaum on 8/6/2018.
 */
class FFRRTypeImplementation32(private val eval: (Float, Float) -> Int) : InstructionImplementation {
    override operator fun invoke(mcode: MachineCode, sim: Simulator) {
        val rs1 = mcode[InstructionField.RS1]
        val rs2 = mcode[InstructionField.RS2]
        val rd = mcode[InstructionField.RD]
        val vrs1 = sim.getFReg(rs1)
        val vrs2 = sim.getFReg(rs2)
        sim.setReg(rd, eval(vrs1, vrs2))
        sim.incrementPC(mcode.length)
    }
}