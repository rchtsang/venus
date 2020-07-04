package venus

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseMap
import kotlinx.serialization.stringify
import venusbackend.utils.SocketMessage
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.experimental.xor
import io.javalin.Javalin
import io.javalin.websocket.WsMessageContext


class ServerSocketHandler(val port: Int, val listener : (WsMessageContext, SocketMessage) -> Unit) {
    val app: Javalin = Javalin.create().start(port)

    init {
        app.ws("/") { ws ->
            ws.onConnect { ctx ->
                println("Client Connected")
            }
            ws.onMessage { ctx ->
                val msg = ctx.message<SocketMessage>()
                listener(ctx, msg)
            }
            ws.onClose { ctx ->
                println("Client closed!")
            }
        }
    }
}