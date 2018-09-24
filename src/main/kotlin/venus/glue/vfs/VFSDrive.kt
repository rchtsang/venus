package venus.glue.vfs

class VFSDrive(val n: String) : VFSFolder(n) {
    override val type = VFSType.Drive
    init {
        //
    }
}