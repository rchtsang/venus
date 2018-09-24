package venus.glue.vfs

class VFSDrive(val n: String) : VFSFolder(n, null) {
    override val type = VFSType.Drive
    override var parent: VFSObject? = null
    init {
        //
    }
}