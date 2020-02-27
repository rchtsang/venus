package venus.vfs

import venus.Driver
import venusbackend.simulator.SimulatorSettings
import java.io.File
import java.nio.file.Paths

class VirtualFileSystem(val defaultDriveName: String, val simSettings: SimulatorSettings = SimulatorSettings()) {
    var sentinel = VFSDrive(defaultDriveName, VFSDummy())
    var currentLocation: VFSObject = sentinel

    companion object {
        val LSName = "VFS_DATA"

        fun getPath(path: String): ArrayList<String> {
            return path.split(VFSObject.separator) as ArrayList
        }
    }

    init {
        currentLocation.parent = currentLocation
    }

    fun makeFileInDir(path: String): VFSFile? {
        // TODO FIX ME
        val f = try {
            File(Paths.get(path).normalize().toUri())
        } catch (e: Exception) {
            return null
        }
        if (!f.exists()) {
            if (!f.createNewFile()) {
                return null
            }
        }
        return if (f.isFile) {
            val vfsf = VFSFile(f.name, VFSDummy())
            vfsf.setFile(f)
            vfsf
        } else {
            null
        }
//        val obj = getObjectFromPath(path)
//        if (obj == null) {
//            val nobj = getObjectFromPath(path, true) as VFSObject
//            return if (nobj.type != VFSType.File) {
//                val name = nobj.label
//                val parent = nobj.parent
//                parent.removeChild(name)
//                val newfile = VFSFile(name, parent)
//                parent.addChild(newfile)
//                newfile
//            } else {
//                nobj as VFSFile
//            }
//        } else {
//            if (obj.type == VFSType.File) {
//                return obj as VFSFile
//            }
//            return null
//        }
    }

    fun reset() {
        this.currentLocation = sentinel
    }

    fun mkdir(dirName: String): String {
        val newdir = VFSFolder(dirName, currentLocation)
        return if (currentLocation.addChild(newdir)) {
            ""
        } else {
            "Could not make directory: $dirName"
        }
    }

    fun cd(dir: String): String {
        val splitpath = getPath(dir)
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

    fun touch(filename: String): String {
        val newfile = VFSFile(filename, currentLocation)
        return if (currentLocation.addChild(newfile)) {
            ""
        } else {
            "Could not make file: $filename"
        }
    }

    fun ls(): String {
        var str = ""
        for (s in currentLocation.contents.keys) {
            str += s + (if ((currentLocation.contents[s] as VFSObject).type in listOf(VFSType.Folder, VFSType.Drive)) VFSObject.separator else "") + "\n"
        }
        return str
    }

    fun cat(filedir: String): String {
        val splitpath = getPath(filedir)
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

    fun path(): String {
        return currentLocation.getPath() + VFSObject.separator
    }
    // @FIXME There is a bug for going into a file
    fun remove(path: String): String {
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

    fun write(path: String, msg: String): String {
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

    fun getParentFromObject(obj: VFSObject): VFSObject? {
        val cf = obj.contents[VFSObject.internalLabelpath] ?: return obj.parent
        var f = File((cf as String))
        val p = f.parentFile
        return getObjectFromPath(p.absolutePath, location = sentinel)
    }

    fun getObjectFromPath(path: String, make: Boolean = false, location: VFSObject? = null): VFSObject? {
        // TODO FIX ME
        val f = try {
            if (location == sentinel) {
                File(path)
            } else {
                File(location?.getPath() ?: Driver.workingdir, path)
            }
        } catch (e: Exception) {
            return null
        }
        return if (f.exists() && f.isFile) {
            val vfsf = VFSFile(f.name, VFSDummy())
            vfsf.setFile(f)
            vfsf.contents[VFSObject.internalLabelpath] = f.absolutePath
            vfsf
        } else if (f.exists() && f.isDirectory) {
            val vfsd = VFSFolder(f.name, VFSDummy())
            vfsd.setFile(f)
            vfsd.contents[VFSObject.internalLabelpath] = f.absolutePath
            vfsd
        } else {
            null
        }
//        val splitpath = getPath(path)
//        var templocation = if (splitpath.size > 0 && splitpath[0].contains(":")) {
//            splitpath.removeAt(0)
//            sentinel
//        } else {
//            currentLocation
//        }
//        for (obj in splitpath) {
//            if (obj == "") {
//                continue
//            }
//            if (!templocation.contents.containsKey(obj)) {
//                if (make) {
//                    templocation.addChild(VFSFile(obj, templocation))
//                } else {
//                    return null
//                }
//            }
//            templocation = templocation.contents[obj] as VFSObject
//        }
//        return templocation
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
}