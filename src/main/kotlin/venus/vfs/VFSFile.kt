package venus.vfs

import java.io.File

class VFSFile(override var label: String, override var parent: VFSObject) : VFSObject {
    override val type = VFSType.File
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    companion object {
        val innerTxt = "innertext"
        val innerFile = "innerfile"
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
        return if (contents.containsKey(innerFile) && contents[innerFile] is File) {
            (contents[innerFile] as File).readText(Charsets.UTF_8)
        } else {
            contents[innerTxt] as String
        }
    }
    fun setText(s: String) {
        if (contents.containsKey(innerFile) && contents[innerFile] is File) {
            (contents[innerFile] as File).writeText(s)
        } else {
            contents[innerTxt] = s
        }
    }

    fun setFile(f: File) {
        contents[innerFile] = f
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