package venus.zip

class Zip {
    var internal_zip = JSZip()
    fun addFile(name: String, data: Any) {
        internal_zip = internal_zip.file(name, data, js("""{"binary":true}"""))
    }

//    fun addFolder()
}

external class JSZip {
    companion object {
        val version: String
    }
    fun file(name: String): JSZip
    fun file(name: String, data: Any, options: Any): JSZip
    fun folder(name: String): JSZip
    fun remove(name: String): JSZip
}