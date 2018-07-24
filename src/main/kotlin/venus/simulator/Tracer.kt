package venus.simulator

import venus.riscv.MachineCode

/**
 * Created by Thaumic on 7/14/2018.
 */
class Tracer (val sim: Simulator) {
    var version = "v2.0.0"
    var format = "%output%%0%\t%1%\t%2%\t%3%\t%4%\t%5%\t%6%\t%7%\t%8%\t%9%\t%10%\t%11%\t%12%\t%13%\t%14%\t%15%\t%16%\t%17%\t%18%\t%19%\t%20%\t%21%\t%22%\t%23%\t%24%\t%25%\t%26%\t%27%\t%28%\t%29%\t%30%\t%31%\t%line%\t%pc%\t%inst%\n"
    var amtReg = 32
    var base = 2
    var totCommands = -1
    var instFirst = false
    private var prevInst = MachineCode(0)
    var maxSteps = 1000000
    var tr = TraceEncapsulation()
    var twoStage = false

    fun trace() {
        this.tr.traced = false
        sim.reset()
        var tl = ArrayList<Trace>()
        var prevTrace: Trace ?= null
        var i = 0
        if (!this.instFirst && !sim.isDone()) {
            prevInst = sim.getNextInstruction()
            sim.step()
        }
        while (!sim.isDone()) {
            if (i > this.maxSteps && this.maxSteps > 0) {
                throw SimulatorError("The max number of steps (" + this.maxSteps + ") in the tracer has been reached! You can increase this in the settings or disable it by setting it to 0 or less. This is the current safty for infinitely looping programs.")
            }
            var currentTrace = getSingleTrace(i)
            currentTrace.prevTrace = prevTrace
            tl.add(currentTrace)
            prevTrace = currentTrace
            sim.step()
            i++
        }
        var currentTrace = getSingleTrace(i)
        currentTrace.prevTrace = prevTrace
        tl.add(currentTrace)
        this.tr.trace = tl
        this.tr.traced = true
        sim.reset()
    }

    fun getSingleTrace(line : Int) : Trace {
        var mc = MachineCode(0)
        if (!sim.isDone()) {
            mc = sim.getNextInstruction()
        }
        if (!this.instFirst) {
            var t = mc
            mc = prevInst
            prevInst = t
        }
        return Trace(didBrach(), getecallMsg(), getRegs(), mc, line, sim.getPC())
    }

    fun getRegs(): IntArray {
        var r = IntArray(amtReg)
        for (i in 0..(amtReg - 1)) {
            r.set(i, sim.getReg(i))
        }
        return r
    }

    fun didBrach(): Boolean {
        return sim.branched
    }

    fun getecallMsg() : String {
        return sim.ecallMsg
    }

    fun getString(): String {
        if (!this.tr.stred) {
            throw SimulatorError("The trace string has not finished!")
        }
        return this.tr.str
    }

    fun traceString() {
        if (!this.tr.traced) {
            throw SimulatorError("You need to make the run the trace before you can get the trace string!")
        }
        this.tr.str = ""
        this.tr.stred = false
        var tr = this.tr.trace
        var i = 0
        cleanFormat()
        for (t in tr) {
            if (twoStage && t.branched) {
                val pt = t.prevTrace
                val flushed = pt?.copy() ?: this.getSingleTrace(-1)
                val nextPC = flushed.pc + flushed.inst.length
                flushed.inst = if (nextPC < this.sim.maxpc) this.sim.linkedProgram.prog.insts[nextPC / flushed.inst.length] else MachineCode(0)
                flushed.pc = nextPC
                flushed.line = i
                this.tr.str += flushed.getString(format, base)
                i++
            }
            t.line = i
            this.tr.str += t.getString(format, base)
            i++
            if (this.totCommands > 0 && i >= this.totCommands) {
                break
            }
        }
        var t = tr[tr.size - 1]
        while (i < this.totCommands) {
            t.inst = MachineCode(0)
            t.line++
            t.pc = incPC(t.pc)
            this.tr.str += t.getString(format, base)
            i++
        }
        this.tr.stred = true
    }

    private fun incPC(pc : Int) : Int {
        return pc + 4
    }

    private fun cleanFormat() {
        this.format = this.format.replace("\\t", "\t").replace("\\n", "\n")
    }

    fun setWordAddressed(b: Boolean) {
        wordAddressed = b
    }

}
class TraceEncapsulation () {

    lateinit var trace: ArrayList<Trace>
    var traced = false
    var str: String = ""
    var stred = false
}
var wordAddressed = false
