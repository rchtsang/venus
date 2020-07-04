package venus

import io.javalin.websocket.WsMessageContext
import venusbackend.utils.SocketMessage

val VERSION = "1.0.0"
class Mounter(var port: String, var dir: String) {
    val socketHandler: ServerSocketHandler
    init {
        socketHandler = ServerSocketHandler(port.toInt(), listener = this::listener)
    }

    fun send(ctx: WsMessageContext, request: SocketMessage, cmd: String, data: Any = "") {
        ctx.send(SocketMessage(v = VERSION, cmd = cmd, id = request.id, data = data))
    }

    fun listener(ctx: WsMessageContext, msg: SocketMessage) {
        when(msg.cmd) {
            "PING" -> {
                println("Ping!")
                this.send(ctx, msg, "PONG")
            }
            "EXIT" -> {
                println("Client exiting!")
            }
        }
    }
}