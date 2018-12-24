package venus.vfs

class JsonContainer {
    var type = "Dummy"
    var label = "DUMMY"
    var contents = ArrayList<JsonContainer>()
    var permissions = VFSPermissions()
    var innerobj: Any = ""
}