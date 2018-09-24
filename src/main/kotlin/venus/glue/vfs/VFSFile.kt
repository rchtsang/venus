package venus.glue.vfs

class VFSFile(var name: String, override var parent: VFSObject?) : VFSObject {
    val innerTxt = "innertext"
    override val type = VFSType.File
    override var label = name
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    init {
        contents[innerTxt] = ""
    }
    fun readText(): String {
        return contents[innerTxt] as String
    }
    fun setText(s: String) {
        contents[innerTxt] = s
    }
}