package venus.simulator.diffs

import venus.riscv.insts.floating.Decimal
import venus.simulator.Diff
import venus.simulator.SimulatorState

class FRegisterDiff(val id: Int, val v: Decimal) : Diff {
    override operator fun invoke(state: SimulatorState) = state.setFReg(id, v)
}