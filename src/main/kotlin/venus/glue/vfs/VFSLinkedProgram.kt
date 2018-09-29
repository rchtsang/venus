package venus.glue.vfs

import venus.linker.LinkedProgram

class VFSLinkedProgram(override var label: String, override var parent: VFSObject, prog: LinkedProgram = LinkedProgram()) : VFSObject {
    override val type = VFSType.LinkedProgram
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    companion object {
        val innerProgram = "innerprogram"
    }
    init {
        contents[innerProgram] = prog
    }
    fun getLinkedProgram(): LinkedProgram {
        return contents[innerProgram] as LinkedProgram
    }
    fun setLinkedProgram(p: LinkedProgram) {
        contents[innerProgram] = p
    }
}