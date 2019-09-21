package venus.vfs

import java.io.File
import java.lang.StringBuilder

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
        val t = if (contents.containsKey(innerFile) && contents[innerFile] is File) {
            val bs = (contents[innerFile] as File).readBytes()
            val sb = StringBuilder()
            for (b in bs) {
                sb.append(b.toChar())
            }
            sb.toString()
        } else {
            contents[innerTxt] as String
        }
        return t
    }
    fun setText(s: String) {
        if (contents.containsKey(innerFile) && contents[innerFile] is File) {
            val f = (contents[innerFile] as File)
            val bytes = ArrayList<Byte>(s.length)
            for (c in s) {
                bytes.add(c.toByte())
            }
            f.writeBytes(bytes.toByteArray())
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