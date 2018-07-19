package venus.simulator

import venus.riscv.MachineCode

import kotlin.js.Math
/**
 * Created by Thaumic on 7/14/2018.
 */

class Trace (branched : Boolean, ecallMsg : String, regs : IntArray, inst : MachineCode, line : Int, pc : Int) {
    var branched = false
    var ecallMsg = ""
    var regs = IntArray(0)
    var inst = MachineCode(0)
    var line = 0
    var pc = 0

    init {
        this.ecallMsg = ecallMsg
        this.branched = branched
        this.regs = regs
        this.inst = inst
        this.line = line
        this.pc = pc
    }

    fun getString(format : String, base : Int) : String {
        if (this.ecallMsg == "exiting the simulator") {
            return "exiting the simulator\n"
        }
        var f = format.replace("%output%", this.ecallMsg).replace("%inst%", numToBase(base, this.inst.toString().toInt(), 32, 10, true)).replace("%pc%", numToBase(base, this.pc, 32, 10, false)).replace("%line%", numToBase(base, this.line, 16, 10, false))
        for (i in 0..(regs.size - 1)) {
            f = f.replace("%" + i.toString() + "%", numToBase(base, this.regs[i], 32, 16, true))
        }
        return f
    }

}
/*
* Takes in a base 10 integer and a base to convert it to and returns a string of what the number is.
*/
external fun numToBase(curNumBase: Int, n : Int, length : Int, base : Int, signextend : Boolean) : String
