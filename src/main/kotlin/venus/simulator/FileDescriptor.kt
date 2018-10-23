package venus.simulator

import venus.glue.vfs.VFSFile

class FileDescriptor(var vfsFile: VFSFile, var parameters: Int) {
    var feof = false
    var ferror = false
    var readOffset = 0
    var writeOffset = 0
    var readable = false
    var writeable = false
    lateinit var dataStream: StringBuilder
    init {
        var dataStream = StringBuilder(vfsFile.readText())
    }

    fun read(size: Int): String? {
        val amtToRead = minOf(size, dataStream.length - readOffset)
        if (amtToRead == 0 || readable) {
            return null
        }

    }

    fun write(value: String): Int {

    }

    fun flush(): Int {
        vfsFile.setText(this.dataStream.toString())
        return 0
    }

    fun isEOF(): Int {
        return if (feof) {
            1
        } else {
            0
        }
    }

    fun getError(): Int {
        return if (ferror) {
            1
        } else {
            0
        }
    }
}