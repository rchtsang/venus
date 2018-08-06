package venus.riscv.insts.dsl.impls.extensions

import venus.riscv.InstructionField
import venus.riscv.MachineCode
import venus.riscv.insts.dsl.impls.InstructionImplementation
import venus.simulator.Simulator

class AMORTypeImplementation32(private val eval: (Int, Int) -> Int) : InstructionImplementation {
    override operator fun invoke(mcode: MachineCode, sim: Simulator) {
        val rs1 = mcode[InstructionField.RS1]
        val rs2 = mcode[InstructionField.RS2]
        val rd = mcode[InstructionField.RD]
        val vrs1 = sim.getReg(rs1)
        val vrs2 = sim.getReg(rs2)

        val data = sim.loadWordwCache(vrs1)
        sim.setReg(rd, data)
        sim.storeWordwCache(vrs1, eval(data, vrs2))
        sim.incrementPC(mcode.length)
    }
}