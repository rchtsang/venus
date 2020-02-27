package venus.vfs

import venus.vfs.VFSObject.Companion.isValidName
import java.io.File

open class VFSFolder(var name: String, override var parent: VFSObject) : VFSObject {
    override val type = VFSType.Folder
    override var label = name
    override var contents = HashMap<String, Any>()
    override var permissions = VFSPermissions()
    init {
        contents[""] = parent
        contents[""] = this
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

    fun setFile(f: File) {
        contents[VFSFile.innerFile] = f
    }

    override fun stringify(): JsonContainer {
        val me = JsonContainer()
        me.label = this.label
        me.permissions = this.permissions
        me.type = this.type.toString()
        for (item in this.contents.keys) {
            if (item !in listOf("", "")) {
                me.contents.add((this.contents[item] as VFSObject).stringify())
            }
        }
        return me
    }
}