package venus.glue.vfs

interface VFSObject {
    val type: VFSType
    var label: String
    var contents: HashMap<String, Any>
    var permissions: VFSPermissions
    var parent: VFSObject
    fun isValidName(name: String): Boolean {
        return !name.contains(separator())
    }
    fun separator(): String {
        return "/"
    }
    fun getPath(): String {
        var path = ""
        var node: VFSObject? = this
        while (node != null && node.type != VFSType.Drive) {
            path += this.separator() + node.label
            node = node.parent
        }
        return node?.label ?: "c:" + path
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
        if (!contents.containsKey(name) || name == ".." || name == ".") {
            return false
        }
        contents.remove(name)
        return true
    }
}