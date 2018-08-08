package venus.riscv.insts.floating.single.i

import venus.riscv.insts.dsl.floating.FITypeInstruction
import venus.riscv.insts.floating.Decimal

/*Single-Precision*/

val flw = FITypeInstruction(
        name = "flw",
        opcode = 0b0000111,
        funct3 = 0b010,
        eval32 = { addr, sim ->
            Decimal(f = Float.fromBits(sim.loadWordwCache(addr)))
        }
)