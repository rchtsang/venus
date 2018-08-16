package venus.assembler.pseudos

import venus.assembler.AssemblerError
import venus.assembler.AssemblerPassOne
import venus.assembler.LineTokens
import venus.assembler.PseudoWriter

/** Writes pseudoinstruction `fmv.d rd, rs` */
object FMVD : PseudoWriter() {
    override operator fun invoke(args: LineTokens, state: AssemblerPassOne): List<LineTokens> {
        if (args[0] !== "fmv.d") {
            throw AssemblerError("The format for this function is wrong!")
        }
        checkArgsLength(args, 3)
        return listOf(listOf("fsgnj.d", args[1], args[2], args[2]))
    }
}