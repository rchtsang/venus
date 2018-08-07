package venus.simulator.diffs

import venus.glue.Renderer
import venus.simulator.Diff
import venus.simulator.SimulatorState

class InstructionDiff(val idx: Int, val mc: Int, val orig: String) : Diff {
    override operator fun invoke(state: SimulatorState) {
        try {
            Renderer.updateProgramListing(idx, mc, orig)
        } catch (e: Throwable) {}
    }
}