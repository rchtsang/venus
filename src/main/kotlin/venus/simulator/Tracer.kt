package venus.simulator

import venus.riscv.MachineCode

/**
 * Created by Thaumic on 7/14/2018.
 */
//@todo Make it so this handles making the string as well.
class Tracer (val sim: Simulator) {
    var format = "%output%%0%\t%1%\t%2%\t%3%\t%4%\t%5%\t%6%\t%7%\t%8%\t%9%\t%10%\t%11%\t%12%\t%13%\t%14%\t%15%\t%16%\t%17%\t%18%\t%19%\t%20%\t%21%\t%22%\t%23%\t%24%\t%25%\t%26%\t%27%\t%28%\t%29%\t%30%\t%31%\t%line%\t%pc%\t%inst%"
    var amtReg = 32
    var base = 16
    var totCommands = -1
    var instFirst = false
    private var prevInst = MachineCode(0)

    fun trace() : ArrayList<Trace> {
        sim.reset()
        var t = ArrayList<Trace>()
        var i = 0
        if (!this.instFirst && !sim.isDone()) {
            prevInst = sim.getNextInstruction()
            sim.step()
        }
        while (!sim.isDone()) {
            t.add(getSingleTrace(i))
            sim.step()
            i++
        }
        t.add(getSingleTrace(i))
        sim.reset()
        return t
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

    fun traceString(): String {
        var tr = this.trace()
        var s = ""
        for (t in tr) {
            s = s + t.getString(format, base) + "\n"
        }
        return s
    }
}