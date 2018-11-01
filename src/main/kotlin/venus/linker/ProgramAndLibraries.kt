package venus.linker

import venus.assembler.AssemblerError
import venus.glue.Driver.assemble
import venus.glue.vfs.VFSFile
import venus.glue.vfs.VFSProgram
import venus.glue.vfs.VFSType
import venus.glue.vfs.VirtualFileSystem
import venus.riscv.Program

class ProgramAndLibraries(val progs: List<Program>, vfs: VirtualFileSystem) {
    val AllProgs = ArrayList<Program>()
    init {
        val seenPrograms = HashSet<String>()
        val needToImportPrograms = HashSet<String>()
        for (prog in progs) {
            seenPrograms.add(prog.name)
            addProgramImports(prog, seenPrograms, needToImportPrograms)
            AllProgs.add(prog)
        }
        while (needToImportPrograms.isNotEmpty()) {
            val progname = needToImportPrograms.elementAt(0)
            needToImportPrograms.remove(progname)
            // Get the file
            val obj = vfs.getObjectFromPath(progname) ?: throw AssemblerError("Could not find the library: $progname")

            val prog = when (obj.type) {
                VFSType.File -> {
                    // Get the text
                    val progText = (obj as VFSFile).readText()
                    val p = assemble(progText) ?: throw AssemblerError("Could not load the library: $progname")
                    p.name = progname
                    p
                }
                VFSType.Program -> {
                    (obj as VFSProgram).getProgram()
                }
                else -> {
                    throw AssemblerError("The path for $progname is not a file or program!")
                }
            }

            // Add program to seen programs and All progs.
            AllProgs.add(prog)
            seenPrograms.add(progname)
            addProgramImports(prog, seenPrograms, needToImportPrograms)
        }
    }

    fun addProgramImports(prog: Program, SeenPrograms: HashSet<String>, needToImportPrograms: HashSet<String>) {
        for (import in prog.imports) {
            if (!SeenPrograms.contains(import)) {
                needToImportPrograms.add(import)
            }
        }
    }
}