package venus.zip

class Zip {
    var internal_zip = JSZip()
    fun addFile(name: String, data: Any) {
        internal_zip = internal_zip.file(name, data, js("""{"binary":true}"""))
    }

    fun save(name: String): String {
        val z = internal_zip
        js("""
            var saveData = (function () {
            var a = document.createElement("a");
            document.body.appendChild(a);
            a.style = "display: none";
            return function (data, fileName) {
                var json = JSON.stringify(data),
                    blob = new Blob([json], {type: "octet/stream"}),
                    url = window.URL.createObjectURL(blob);
                a.href = url;
                a.download = fileName;
                a.click();
                window.URL.revokeObjectURL(url);
            };
        }());

           z.generateAsync({"type": "blob"}).then(function(data){
            saveData(data, name);
           });
        """)
        return ""
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