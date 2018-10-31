package venus.glue.jvm

import venus.riscv.insts.integer.base.i.*
import venus.riscv.insts.integer.base.r.*
import venus.riscv.insts.integer.base.sb.*
import venus.riscv.insts.integer.base.s.*
import venus.riscv.insts.integer.base.u.*
import venus.riscv.insts.integer.base.uj.*
import venus.riscv.insts.integer.extensions.atomic.r.*
import venus.riscv.insts.integer.extensions.multiply.r.*

class JVMInitInstructions {
    companion object {
        var inited = false
    }
    init {
        if (!inited) {
            addi
            addiw
            andi
            csrrc
            csrrci
            csrrw
            csrrwi
            ebreak
            ecall
            fence
            fencei
            jalr
            lb
            lbu
            ld
            lh
            lhu
            lw
//            lwu
            ori
            slli
            slliw
            slti
            sltiu
            srai
            sraiw
            srli
            srliw
            xori
            add
            addw
            and
            or
            sll
            sllw
            slt
            sltu
            sra
            sraw
            srl
            srlw
            sub
            subw
            xor
            sb
            sd
            sh
            sw
            beq
            bge
            bgeu
            blt
            bltu
            bne
            auipc
            lui
            jal
            amoaddw
            amoandw
            amomaxw
            amomaxuw
            amominw
            amominuw
            amoorw
            amoswapw
            amoxorw
            lrw
            scw
            div
            divu
//            divw
            mul
            mulh
            mulhsu
            mulhu
//            mulw
            rem
            remu
//            remuw
//            remw
            inited = true
        }
    }
}