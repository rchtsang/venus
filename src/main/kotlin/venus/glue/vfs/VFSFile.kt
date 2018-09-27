package venus.glue.vfs

class VFSFile(override var label: String, override var parent: VFSObject) : VFSObject {
    override val type = VFSType.File
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    companion object {
        val innerTxt = "innertext"
    }
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