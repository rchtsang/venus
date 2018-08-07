package venus.simulator.diffs

import venus.riscv.Address
import venus.simulator.Diff
import venus.simulator.SimulatorState

class CacheDiff(val addr: Address) : Diff {
    override operator fun invoke(state: SimulatorState) {
        state.cache.undoAccess(addr)
    }
}