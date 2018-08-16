package venus.assembler.pseudos

import venus.assembler.AssemblerError
import venus.assembler.AssemblerPassOne
import venus.assembler.LineTokens
import venus.assembler.PseudoWriter

/** Writes pseudoinstruction `fmv.s rd, rs` */
object FNEGS : PseudoWriter() {
    override operator fun invoke(args: LineTokens, state: AssemblerPassOne): List<LineTokens> {
        if (args[0] !== "fneg.s") {
            throw AssemblerError("The format for this function is wrong!")
        }
        checkArgsLength(args, 3)
        return listOf(listOf("fsgnjn.s", args[1], args[2], args[2]))
    }
}