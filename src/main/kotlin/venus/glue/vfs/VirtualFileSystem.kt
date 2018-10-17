package venus.glue.vfs

import venus.glue.Driver
import venus.simulator.SimulatorSettings

@JsName("VirtualFileSystem") class VirtualFileSystem(val defaultDriveName: String, val simSettings: SimulatorSettings = SimulatorSettings()) {
    var sentinel = VFSDrive(defaultDriveName, VFSDummy())
    var currentLocation: VFSObject = sentinel

    companion object {
        val LSName = "VFS_DATA"
    }

    init {
        currentLocation.parent = currentLocation
    }

    @JsName("reset") fun reset() {
        this.currentLocation = sentinel
    }

    @JsName("mkdir") fun mkdir(dirName: String): String {
        val newdir = VFSFolder(dirName, currentLocation)
        return if (currentLocation.addChild(newdir)) {
            ""
        } else {
            "Could not make directory: $dirName"
        }
    }

    @JsName("cd") fun cd(dir: String): String {
        val splitpath = dir.split(VFSObject.separator) as MutableList
        var templocation = if (splitpath.size > 0 && splitpath[0].contains(":")) {
            splitpath.removeAt(0)
            sentinel
        } else {
            currentLocation
        }
        for (dir in splitpath) {
            if (dir == "") {
                continue
            }
            if (!templocation.contents.containsKey(dir)) {
                return "cd: $dir: No such file or directory"
            }
            templocation = templocation.contents.get(dir) as VFSObject
            if (templocation.type in listOf(VFSType.File, VFSType.Program)) {
                return "cd: $dir: Not a directory"
            }
        }
        currentLocation = templocation
        return ""
    }

    @JsName("touch") fun touch(filename: String): String {
        val newfile = VFSFile(filename, currentLocation)
        return if (currentLocation.addChild(newfile)) {
            ""
        } else {
            "Could not make file: $filename"
        }
    }

    @JsName("ls") fun ls(): String {
        var str = ""
        for (s in currentLocation.contents.keys) {
            str += s + (if ((currentLocation.contents[s] as VFSObject).type in listOf(VFSType.Folder, VFSType.Drive)) VFSObject.separator else "") + "\n"
        }
        return str
    }

    @JsName("cat") fun cat(filedir: String): String {
        val splitpath = filedir.split(VFSObject.separator) as MutableList
        var templocation = if (splitpath.size > 0 && splitpath[0].contains(":")) {
            splitpath.removeAt(0)
            sentinel
        } else {
            currentLocation
        }
        for (obj in splitpath) {
            if (obj == "") {
                continue
            }
            if (!templocation.contents.containsKey(obj)) {
                return "cat: $filedir: No such file or directory"
            }
            templocation = templocation.contents[obj] as VFSObject
        }
        if (templocation.type != VFSType.File) {
            return "cat: $filedir: Is not a file!"
        }
        if (!templocation.contents.containsKey(VFSFile.innerTxt)) {
            return "cat: $filedir: COULD NOT FIND FILE CONTENTS!"
        }
        return templocation.contents.get(VFSFile.innerTxt) as String
    }

    @JsName("path") fun path(): String {
        return currentLocation.getPath() + VFSObject.separator
    }
    // @FIXME There is a bug for going into a file
    @JsName("remove") fun remove(path: String): String {
        val templocation = this.getObjectFromPath(path)
        if (templocation === null) {
            return "rm: cannot remove '$path': No such file or directory"
        }
        if (currentLocation.getPath().contains(templocation.getPath())) {
            return "rm: cannot remove '$path': Path is currently active"
        }
        val p = templocation.parent

        return (if (p.removeChild(templocation.label)) "" else "rm: could not remove file")
    }

    @JsName("write") fun write(path: String, msg: String): String {
        val splitpath = path.split(VFSObject.separator) as MutableList
        var templocation = if (splitpath.size > 0 && splitpath[0] == sentinel.label) {
            splitpath.removeAt(0)
            sentinel
        } else {
            currentLocation
        }
        for (obj in splitpath) {
            if (obj == "") {
                continue
            }
            if (!templocation.contents.containsKey(obj)) {
                return "write: cannot write to '$path': No such file or directory"
            }
            templocation = templocation.contents[obj] as VFSObject
        }
        if (templocation.type != VFSType.File) {
            return "cat: $path: Is not a file!"
        }
        (templocation as VFSFile).setText(msg)
        return ""
    }

    fun getObjectFromPath(path: String): VFSObject? {
        val splitpath = path.split(VFSObject.separator) as MutableList
        var templocation = if (splitpath.size > 0 && splitpath[0].contains(":")) {
            splitpath.removeAt(0)
            sentinel
        } else {
            currentLocation
        }
        for (obj in splitpath) {
            if (obj == "") {
                continue
            }
            if (!templocation.contents.containsKey(obj)) {
                return null
            }
            templocation = templocation.contents[obj] as VFSObject
        }
        return templocation
    }

    fun filesFromPrefix(prefix: String): ArrayList<String> {
        val fnames = ArrayList<String>()
        for (key: String in this.currentLocation.contents.keys) {
            if (key.startsWith(prefix)) {
                val obj = this.currentLocation.contents[key] as VFSObject
                var k = key
                if (obj.type in listOf(VFSType.Folder, VFSType.Drive)) {
                    k += "/"
                }
                fnames.add(k)
            }
        }
        return fnames
    }

    fun stringify(): String {
        return JSON.stringify(sentinel.stringify())
    }

    fun parse(vfsString: String) {
        val raw = JSON.parse<JsonContainer>(vfsString)
        val temp = VFSDrive.inflate(raw, VFSDummy())
        val newsent = temp as VFSDrive
        newsent.parent = newsent
        this.sentinel = newsent
        this.currentLocation = this.sentinel
    }

    fun load() {
        val orig = "NULL"
        val vfsJSON = Driver.LS.safeget(LSName, orig)
        if (vfsJSON != orig) {
            this.parse(vfsJSON)
        }
    }

    fun save() {
        if (Driver.useLS) {
            val vfsJSON = this.stringify()
            Driver.LS.set(LSName, vfsJSON)
        }
    }
}