package venus.assembler.pseudos

import venus.assembler.AssemblerError
import venus.assembler.AssemblerPassOne
import venus.assembler.LineTokens
import venus.assembler.PseudoWriter

/** Writes pseudoinstruction `sext.w rd, rs` */
object SEXTW : PseudoWriter() {
    override operator fun invoke(args: LineTokens, state: AssemblerPassOne): List<LineTokens> {
        if (args[0] !== "sext.w") {
            throw AssemblerError("The format for this function is wrong!")
        }
        checkArgsLength(args, 3)
        return listOf(listOf("addiw", args[1], args[2], "0"))
    }
}