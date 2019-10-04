package venus.vfs

import venus.Driver
import venusbackend.simulator.SimulatorSettings
import kotlin.browser.window

@JsName("VirtualFileSystem") class VirtualFileSystem(val defaultDriveName: String, val simSettings: SimulatorSettings = SimulatorSettings()) {
    var sentinel = VFSDrive(defaultDriveName, VFSDummy())
    var currentLocation: VFSObject = sentinel

    companion object {
        val LSName = "VFS_DATA"

        fun getPath(path: String): ArrayList<String> {
            return path.split(VFSObject.separator) as ArrayList
        }
    }

    init {
        sentinel.parent = sentinel
    }

    fun makeFileInDir(path: String): VFSFile? {
        val obj = getObjectFromPath(path)
        if (obj == null) {
            val nobj = getObjectFromPath(path, true) as VFSObject
            return if (nobj.type != VFSType.File) {
                val name = nobj.label
                val parent = nobj.parent
                parent.removeChild(name)
                val newfile = VFSFile(name, parent)
                parent.addChild(newfile)
                newfile
            } else {
                nobj as VFSFile
            }
        } else {
            if (obj.type == VFSType.File) {
                return obj as VFSFile
            }
            return null
        }
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
        var tmp = chdir(dir, currentLocation)
        if (tmp is VFSObject) {
            if (tmp.type !in listOf(VFSType.Folder, VFSType.Drive)) {
                return "Can only go into folders and drives."
            }
            currentLocation = tmp
        } else {
            return tmp.toString()
        }
        return ""
    }

    fun chdir(dir: String, curloc: VFSObject): Any {
        val splitpath = getPath(dir)
        var templocation = if (dir.startsWith("/") || dir == "") {
            if (splitpath.size > 0) {
                splitpath.removeAt(0)
            }
            sentinel
        } else {
            curloc
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
        return templocation
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
        val splitpath = getPath(filedir)
        var templocation = if (filedir.startsWith("/")) {
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
        return rm(path, currentLocation)
    }

    fun rm(path: String, curloc: VFSObject): String {
        val templocation = this.getObjectFromPath(path)
        if (templocation === null) {
            return "rm: cannot remove '$path': No such file or directory"
        }
        if (curloc.getPath().contains(templocation.getPath())) {
            return "rm: cannot remove '$path': Path is currently active"
        }
        val p = templocation.parent

        return (if (p.removeChild(templocation.label)) "" else "rm: could not remove file")
    }

    @JsName("write") fun write(path: String, msg: String): String {
        val splitpath = getPath(path)
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

    @JsName("addFile") fun addFile(path: String, data: String, loc: VFSObject = currentLocation): String {
        val splitpath = getPath(path)
        if (splitpath.size == 0) {
            return "There was no file passed in!"
        }
        val fname = splitpath.removeAt(splitpath.size - 1)
        var curloc = loc
        for (p in splitpath) {
            curloc = if (curloc.contents.containsKey(p)) {
                val next = curloc.contents[p]!! as VFSObject
                if (next.type !in listOf(VFSType.Folder, VFSType.Drive)) {
                    return "Could not create folder due to a non folder existing in the path."
                }
                next
            } else {
                val fold = VFSFolder(p, curloc)
                curloc.addChild(fold)
                fold
            }
        }
        val f = VFSFile(fname, curloc)
        f.setText(data)
        curloc.addChild(f)
        return ""
    }

    fun getObjectFromPath(path: String, make: Boolean = false, location: VFSObject? = null): VFSObject? {
        val splitpath = getPath(path)
        var templocation = if (path.startsWith("/")) {
            splitpath.removeAt(0)
            sentinel
        } else {
            location ?: currentLocation
        }
        for (obj in splitpath) {
            if (obj == "") {
                continue
            }
            if (!templocation.contents.containsKey(obj)) {
                if (make) {
                    templocation.addChild(VFSFile(obj, templocation))
                } else {
                    return null
                }
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
        val vfsJSON = window.localStorage.getItem(LSName)
        if (vfsJSON != undefined) {
            this.parse(vfsJSON)
        }
    }

    fun save() {
        if (Driver.useLS) {
            val vfsJSON = this.stringify()
            window.localStorage.setItem(LSName, vfsJSON)
        }
    }
}