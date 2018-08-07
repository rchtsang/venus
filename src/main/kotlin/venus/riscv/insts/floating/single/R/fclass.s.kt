package venus.riscv.insts.floating.single.r

import venus.riscv.insts.dsl.floating.FclassRTypeInstruction

/*Single-Precision*/
/*TODO fix so this is better.*/
val fclass = FclassRTypeInstruction(
        name = "fclass.s",
        opcode = 0b1010011,
        funct3 = 0b001,
        funct7 = 0b1110000,
        eval32 = { a, b ->
            var bits = 0b0
            if (a == Float.NEGATIVE_INFINITY) bits = bits or 0b0000000001
            if (a < 0 && a >= (1.17549435e-38).toFloat()) bits = bits or 0b0000000010
            if (a < 0 && a < (1.17549435e-38).toFloat()) bits = bits or 0b0000000100
            if (a == 0F) bits = bits or 0b0000001000
            if (a == -0F) bits = bits or 0b0000010000
            if (a >= (1.17549435e-38).toFloat()) bits = bits or 0b0000100000
            if (a < (1.17549435e-38).toFloat()) bits = bits or 0b0001000000
            if (a == Float.POSITIVE_INFINITY) bits = bits or 0b0010000000
            if (Float.NaN.equals(a)) bits = bits or 0b0100000000
            if (Float.NaN.equals(a)) bits = bits or 0b1000000000
            bits
        }
)