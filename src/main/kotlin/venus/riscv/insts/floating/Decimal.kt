package venus.riscv.insts.floating

import kotlin.math.abs

class Decimal(f: Float = 0F, d: Double = 0.0, isF: Boolean = true) {
    var float: Float = f
        private set
    var double: Double = d
        private set
    var isFloat: Boolean = isF
    fun isDouble(): Boolean = !this.isFloat

    fun set(float: Float) {
        this.float = float
        isFloat = true
    }

    fun set(double: Double) {
        this.double = double
        isFloat = false
    }

    fun get(): Number {
        if (this.isFloat) {
            return this.float
        } else {
            return this.double
        }
    }

    fun getFloat(): Float {
        if (this.isFloat) {
            return this.float
        } else {
            var s = this.double.toRawBits().toString(16)
            s += "0".repeat(16 - s.length)
            return Float.fromBits(s.substring(8 until 16).toInt(16))
        }
    }

    fun getDouble(): Double {
        if (this.isFloat) {
            var s = this.float.toRawBits().toString(16)
            s += "0".repeat(8 - s.length)
            s = "0".repeat(8) + s
            return Double.fromBits(s.toLong(16))
        } else {
            return this.double
        }
    }

    fun toHex(): String {
        return this.toDecimal()
        /*FIXME make it convert to hex correctly*/
        var s: String
        if (this.isFloat) {
            s = this.float.toRawBits().toString(16)
            s = s.removePrefix("-")
            s = "0x" + s + "0".repeat(8 - s.length)
        } else {
            s = this.double.toRawBits().toString(16)
            s = s.removePrefix("-")
            s = "0x" + s + "0".repeat(16 - s.length)
        }
        return s
    }

    fun toDecimal(): String {
        var s = ""
        if (this.isFloat) {
            if (this.float.toRawBits() == 0x80000000.toInt()) {
                s = "-"
            }
            s += this.float.toString()
        } else {
            if (this.double.toRawBits().toString(16) == "8000000000000000") {
                s = "-"
            }
            s += this.double.toString()
        }
        return s
    }

    fun toUnsigned(): String {
        val s: String
        if (this.isFloat) {
            s = abs(this.float).toString()
        } else {
            s = abs(this.double).toString()
        }
        return s
    }

    fun toAscii(): String {
        return this.toHex()
    }
}