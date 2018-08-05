package venus.simulator.diffs

import venus.simulator.Diff
import venus.simulator.SimulatorState

class FRegisterDiff(val id: Int, val v: Int) : Diff {
    override operator fun invoke(state: SimulatorState) = state.setFReg(id, v)
}