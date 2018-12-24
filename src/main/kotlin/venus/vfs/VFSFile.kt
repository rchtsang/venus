package venus.vfs

class VFSFile(override var label: String, override var parent: VFSObject) : VFSObject {
    override val type = VFSType.File
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    companion object {
        val innerTxt = "innertext"
        fun inflate(jsonContainer: JsonContainer, parent: VFSObject): VFSObject {
            val file = VFSFile(jsonContainer.label, parent)
            file.setText(jsonContainer.innerobj as String)
            return file
        }
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

    override fun stringify(): JsonContainer {
        val me = JsonContainer()
        me.label = this.label
        me.permissions = this.permissions
        me.type = this.type.toString()
        me.innerobj = this.contents[innerTxt] as String
        return me
    }
}