package venus

import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import venusbackend.utils.SocketMessage
import kotlin.browser.window

class WebSocketHandler(val url: String, val listener: (SocketMessage) -> Unit, val reconnectDelayMillis: Int = 5000, var reconnectAttempts: Int = 5, var resendMillis: Int = 5000) {
    private var currentSocket: WebSocket? = null
    private val queue = arrayListOf<String>()
    private var closed = false
    var attempts = 1
    private var msgID: Int = 1
    private var callbackMap: MutableMap<Int, (SocketMessage) -> Unit> = HashMap()
    private var unackedMessage: MutableMap<Int, SocketMessage> = HashMap()

    init {
        connect()
    }

    fun entriesOf(jsObject: dynamic): List<Pair<String, Any?>> =
            (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
                    .invoke(jsObject)
                    .map { entry -> entry[0] as String to entry[1] }

    fun mapOf(jsObject: dynamic): Map<String, Any?> =
            entriesOf(jsObject).toMap()

    fun stop() {
        closed = true
        try {
            currentSocket?.close()
        } catch (ignore: Throwable) {
            currentSocket = null
        }
    }

    fun send(o: SocketMessage, callback: ((SocketMessage) -> Unit)? = null) {
        if (closed) {
            throw IllegalStateException("Socket already stopped")
        }

        o.id = msgID
        msgID++

        unackedMessage[o.id] = o
        if (callback != null) {
            callbackMap[o.id] = callback
        }

        window.setTimeout(
            this::resend,
            resendMillis,
            o.id
        )

        val text = JSON.stringify(o)
        queue.add(text)

        flush()
    }

    fun resend(smId: Int) {
        if (smId in unackedMessage) {
            val o = unackedMessage[smId] ?: return
            window.setTimeout(
                    this::resend,
                    resendMillis,
                    o.id
            )
            val text = JSON.stringify(o)
            queue.add(text)
            flush()
        }
    }

    private fun flush() {
        val s = currentSocket ?: return

        try {
            val iterator = queue.iterator()
            while (iterator.hasNext()) {
                val text = iterator.next()

                s.send(text)

                iterator.remove()
            }
        } catch (ignore: Throwable) {
        }
    }

    private fun onMessage(message: dynamic) {
        val socketMessage = SocketMessage.parse(message)
        if (socketMessage.id in unackedMessage) {
            unackedMessage.remove(socketMessage.id)
        }
        callbackMap[socketMessage.id]?.let { it(socketMessage) } ?: listener(socketMessage)
        callbackMap.remove(socketMessage.id)
    }

    private fun reconnectWithDelay() {
        window.setTimeout(this::connect, reconnectDelayMillis)
    }

    private fun connect() {
        try {
            tryConnect()
        } catch (any: Throwable) {
            reconnectWithMessageAndDelay()
        }
    }

    private fun reconnectWithMessageAndDelay() {
        console.error("WebSocket ($url) connection failure!")
        console.error("Will reconnect in ${reconnectDelayMillis / 1000.0}s")
        reconnectWithDelay()
    }

    private fun tryConnect() {
        if (attempts > reconnectAttempts) {
            console.error("Exceeded max reconnect attempts!")
            return
        }
        attempts++
        val socket = WebSocket(url)
        fun closeSocket() {
            try {
                currentSocket?.close()
                socket.close()
            } finally {
                currentSocket = null
            }
        }

        socket.onopen = {
            if (currentSocket !== socket) {
                currentSocket?.close()
                currentSocket = socket
            }

            window.setTimeout({
                flush()
            }, 0)
        }

        socket.onerror = {
            closeSocket()
            reconnectWithMessageAndDelay()
        }

        socket.onmessage = {
            if (it is MessageEvent) {
                val data = it.data
                console.log("Received from websocket", data)
                if (data is String) {
                    val jsDict = JSON.parse<MutableMap<String, Any>>(data)

                    onMessage(mapOf(jsDict))
                }

                flush()
            }
        }

        socket.onclose = {
            if (socket === currentSocket) {
                currentSocket = null
            }

            if (!closed) {
                tryConnect()
            }
        }
    }
}