package venus.glue.vfs

@JsName("VirtualFileSystem") class VirtualFileSystem(val defaultDriveName: String) {
    val sentinel = VFSDrive(defaultDriveName, VFSDummy())
    var currentLocation: VFSObject = sentinel
    init {
        currentLocation.parent = currentLocation
    }

    @JsName("mkdir") fun mkdir(dirName: String): Boolean {
        val newdir = VFSFolder(dirName, currentLocation)
        return currentLocation.addChild(newdir)
    }

    @JsName("cd") fun cd(dir: String): Boolean {
        val splitpath = dir.split(currentLocation.separator())
        var templocation = currentLocation
        for (dir in splitpath) {
            if (!templocation.contents.containsKey(dir)) {
                return false
            }
            templocation = templocation.contents.get(dir) as VFSObject
        }
        currentLocation = templocation
        return true
    }

    @JsName("touch") fun touch(filename: String): Boolean {
        val newfile = VFSFile(filename, currentLocation)
        return currentLocation.addChild(newfile)
    }

    @JsName("ls") fun ls(): String {
        var str = ""
        for (s in currentLocation.contents.keys) {
            str += s + "\n"
        }
        return str
    }

    @JsName("cat") fun cat(filedir: String): String {
        val splitpath = filedir.split(currentLocation.separator())
        var templocation = currentLocation
        for (obj in splitpath) {
            if (!templocation.contents.containsKey(obj)) {
                return "cat: $filedir: No such file or directory"
            }
        }
        if (templocation.type != VFSType.File) {
            return "cat: $filedir: Is a directory"
        }
        if (!templocation.contents.containsKey(VFSFile.innerTxt)) {
            return "cat: $filedir: COULD NOT FIND FILE CONTENTS!"
        }
        return templocation.contents.get(VFSFile.innerTxt) as String
    }
}