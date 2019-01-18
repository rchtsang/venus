package venus.api.venusbackend.simulator

import venusbackend.riscv.insts.floating.Decimal
import venusbackend.simulator.Simulator

@JsName("Simulator") object Simulator {
    @JsName("isDone") fun isDone(sim: Simulator): Boolean {
        return sim.isDone()
    }

    @JsName("getCycles") fun getCycles(sim: Simulator): Int {
        return sim.getCycles()
    }

    @JsName("addArg") fun addArg(sim: Simulator, arg: String) {
        sim.addArg(arg)
    }

    @JsName("step") fun step(sim: Simulator) {
        sim.step()
    }

    @JsName("getReg") fun getReg(sim: Simulator, id: Int): Number {
        return sim.getReg(id)
    }

    @JsName("getFReg") fun getFReg(sim: Simulator, id: Int): Decimal {
        return sim.getFReg(id)
    }

    @JsName("loadByte") fun loadByte(sim: Simulator, addr: Number): Int {
        return sim.loadByte(addr)
    }

    @JsName("reset") fun reset(sim: Simulator) {
        sim.reset()
    }
}