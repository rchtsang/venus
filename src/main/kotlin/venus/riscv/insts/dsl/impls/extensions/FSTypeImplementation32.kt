package venus.riscv.insts.dsl.impls.extensions

import venus.riscv.MachineCode
import venus.riscv.insts.dsl.impls.InstructionImplementation
import venus.simulator.Simulator

/**
 * Created by thaum on 8/6/2018.
 */
class FSTypeImplementation32(private val eval: (Int, Int) -> Int) : InstructionImplementation {
    override operator fun invoke(mcode: MachineCode, sim: Simulator) {
        throw NotImplementedError("Working on floating impls.")
    }
}