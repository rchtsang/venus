package venus.vfs

import venus.WebSocketHandler
import venusbackend.utils.SocketMessage

val VERSION = "1.0.0"
class VFSMountedDriveHandler(url: String) {
    val ws = WebSocketHandler(url, listener = this::listener, reconnectDelayMillis = 5000)

    init {
//        this.send("PING", callback = { sm ->
//            ponged(sm)
//            this.send("PING", callback = { sm ->
//                listener(sm)
//                this.send("PING", callback = this::ponged)
//            })
//        })
    }

    fun send(cmd: String, data: Any = "", callback: ((SocketMessage) -> Unit)? = null) {
        console.log("Sending $cmd!")
        this.ws.send(SocketMessage(v = VERSION, cmd = cmd, data = data), callback = callback)
    }

    fun sendAndWait(cmd: String, data: Any = "", callback: ((SocketMessage) -> Unit)? = null) {
        this.send(cmd = cmd, data = data, callback = callback)

    }

    fun close() {
        ws.stop()
    }

    /*
    * Required fields are:
    * cmd = command
    * v = version
    *
    * */
    fun listener(data: SocketMessage) {
        val cmd = data.cmd
        console.log(cmd)
        console.log(data)
    }

    fun ponged(sm: SocketMessage) {
        console.log("Ponged!\n" + sm)
    }


}