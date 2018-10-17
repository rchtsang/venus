package venus.glue.vfs

class VFSDrive(val n: String, override var parent: VFSObject) : VFSFolder(n, parent) {
    override val type = VFSType.Drive
    init {
        this.contents[".."] = this
        this.label += ":"
    }

    companion object {
        fun inflate(jsonContainer: JsonContainer, parent: VFSObject): VFSObject {
            val folder = VFSDrive(jsonContainer.label.removeSuffix(":"), parent)
            for (i in 0 until js("jsonContainer.contents.length")) {
                val value = js("jsonContainer.contents[i]")
                val obj = when (value.type) {
                    VFSType.Program.toString() -> { VFSProgram.inflate(value, folder) }
                    VFSType.Folder.toString() -> { VFSFolder.inflate(value, folder) }
                    VFSType.LinkedProgram.toString() -> { VFSLinkedProgram.inflate(value, folder) }
                    VFSType.File.toString() -> { VFSFile.inflate(value, folder) }
                    VFSType.Drive.toString() -> { VFSDrive.inflate(value, folder) }
                    else -> { VFSDummy() }
                }
                folder.addChild(obj)
            }
            return folder
        }
    }
}