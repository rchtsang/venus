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
                var add = true
                val obj = when (value.type) {
                    VFSType.Program.toString() -> {
                        add = false
                        VFSProgram.inflate(value, folder)
                    }
                    VFSType.Folder.toString() -> { VFSFolder.inflate(value, folder) }
                    VFSType.LinkedProgram.toString() -> {
                        add = false
                        VFSLinkedProgram.inflate(value, folder)
                    }
                    VFSType.File.toString() -> { VFSFile.inflate(value, folder) }
                    VFSType.Drive.toString() -> { VFSDrive.inflate(value, folder) }
                    else -> { VFSDummy() }
                }
                if (add) {
                    folder.addChild(obj)
                }
            }
            return folder
        }
    }
}