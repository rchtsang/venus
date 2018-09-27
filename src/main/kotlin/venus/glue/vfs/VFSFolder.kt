package venus.glue.vfs

open class VFSFolder(var name: String, override var parent: VFSObject) : VFSObject {
    override val type = VFSType.Folder
    override var label = name
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    init {
        contents[".."] = parent
        contents["."] = this
    }
    fun addFile(name: String): Boolean {
        if (isValidName(name) && !contents.containsKey(name)) {
            contents[name] = VFSFile(name, this)
            return true
        }
        return false
    }
    fun addFolder(name: String): Boolean {
        if (isValidName(name) && !contents.containsKey(name)) {
            contents[name] = VFSFolder(name, this)
            return true
        }
        return false
    }
}