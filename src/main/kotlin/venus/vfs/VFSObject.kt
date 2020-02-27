package venus.vfs

import java.io.File

interface VFSObject {
    val type: VFSType
    var label: String
    var contents: HashMap<String, Any>
    var permissions: VFSPermissions
    var parent: VFSObject
    companion object {
        fun isValidName(name: String): Boolean {
            return !name.contains(Regex("[" + separator + ":\"><]"))
        }
        const val separator = "/"
        const val internalLabelpath = "VENUS_INTERNAL_LABEL-fpath"
    }

    fun getPath(): String {
        if (this.contents.containsKey(internalLabelpath)) {
            return this.contents[internalLabelpath] as String
        }
        var path = ""
        var node: VFSObject? = this
        while (node != null && node.type != VFSType.Drive) {
            path = separator + node.label + path
            node = node.parent
        }
        return ((node?.label ?: "/") + path)
    }

    fun addChild(child: VFSObject): Boolean {
        if (this.contents.containsKey(child.label) || !isValidName(child.label)) {
            return false
        } else {
            this.contents.put(child.label, child)
            return true
        }
    }

    fun removeChild(name: String): Boolean {
        if (!contents.containsKey(name) || name == "" || name == "") {
            return false
        }
        contents.remove(name)
        return true
    }
    fun stringify(): JsonContainer {
        val me = JsonContainer()
        me.label = this.label
        me.permissions = this.permissions
        me.type = this.type.toString()
        return me
    }
}