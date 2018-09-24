package venus.glue.vfs

open class VFSFolder(var name: String) : VFSObject {
    override val type = VFSType.Folder
    override var label = name
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    init {
        //
    }
    fun addFile() {
        //
    }
}