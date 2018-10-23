package venus.simulator

class FilesHandler(sim: Simulator) {
    companion object {
        var EOF = -1
    }
    val files = HashMap<Int, FileDescriptor>()
    var fdCounter = 1

    fun openFile(sim: Simulator, filename: String, permissions: Int): Int {
        // @TODO Make me work!
        return EOF
    }

    fun getFileDescriptor(fdID: Int): FileDescriptor? {
        if (files.containsKey(fdID)) {
            return files[fdID]
        }
        return null
    }

    fun closeFileDescriptor(fdID: Int): Int {
        val fd = files.remove(fdID)
        return fd?.flush() ?: EOF
    }

    fun flushFileDescriptor(fdID: Int): Int {
        val fd = getFileDescriptor(fdID)
        return fd?.flush() ?: EOF
    }

    fun readFileDescriptor(fdID: Int, size: Int): String? {
        val fd = getFileDescriptor(fdID)
        return fd?.read(size)
    }

    fun writeFileDescriptor(fdID: Int, value: String): Int {
        val fd = getFileDescriptor(fdID)
        return fd?.write(value) ?: EOF
    }

    fun getFileDescriptorEOF(fdID: Int): Int {
        val fd = getFileDescriptor(fdID)
        return fd?.isEOF() ?: EOF
    }

    fun getFileDescriptorError(fdID: Int): Int {
        val fd = getFileDescriptor(fdID)
        return fd?.getError() ?: EOF
    }
}