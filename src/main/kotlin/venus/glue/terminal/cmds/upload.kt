package venus.glue.terminal.cmds

import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import venus.glue.terminal.Command
import venus.glue.terminal.Terminal
import kotlin.browser.document
import kotlin.browser.window

var upload = Command(
        name = "upload",
        execute = fun (args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            val uploadbtn = document.getElementById("venus_upload") as HTMLInputElement
            uploadbtn.click()
            window.setTimeout({ e: HTMLInputElement, t: Terminal -> handleFileUploads(e, t) }, 100, uploadbtn, t)
            return ""
        }
)

fun handleFileUploads(e: HTMLInputElement, t: Terminal) {
    if (e.files !== null && (e.files as FileList).length > 0) {
        val files = (e.files as FileList)
        for (i in 0 until files.length) {
            val file = files[i] as File
            setup_reader(file)
        }
        /* This is to clear the upload button of files. */
        e.value = ""
    } else {
        window.setTimeout({ e: HTMLInputElement, t: Terminal -> handleFileUploads(e, t) }, 100, e, t)
    }
}

fun setup_reader(file: File) {
    var name = file.name
    var reader = FileReader()
    js("""reader.onload = function(e){
            var contents = e.target.result;
            driver.VFS.touch(name);
            driver.VFS.write(name, contents);
        }""")
    reader.readAsBinaryString(file)
}