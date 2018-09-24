package venus.glue.vfs

interface VFSObject {
    val type: VFSType
    var label: String
    var contents: HashMap<String, Any>
    var permissions: VFSPermissions
    var parent: VFSObject?
    fun isValidName(name: String): Boolean {
        return true
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
}