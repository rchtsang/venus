package venus.glue.vfs

import venus.riscv.Program

class VFSProgram(override var label: String, override var parent: VFSObject, prog: Program = Program()) : VFSObject {
    override val type = VFSType.Program
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    companion object {
        val innerProgram = "innerprogram"
    }
    init {
        contents[innerProgram] = prog
    }
    fun getProgram(): Program {
        return contents[innerProgram] as Program
    }
    fun setProgram(p: Program) {
        contents[innerProgram] = p
    }
}