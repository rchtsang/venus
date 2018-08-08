package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.F3RTypeInstruction
import venus.riscv.insts.floating.Decimal
import kotlin.math.sign
import kotlin.math.withSign

/*Single-Precision*/
val fsgnjxs = F3RTypeInstruction(
        name = "fsgnjx.s",
        opcode = 0b1010011,
        funct7 = 0b0010000,
        funct3 = 0b010,
        eval32 = { a, b -> Decimal(f = a.getFloat().withSign((a.getFloat().toRawBits() xor b.getFloat().toRawBits()).sign)) }
)