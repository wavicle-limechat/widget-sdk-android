package ai.limechat.widget.characterization

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * Minimal blocking http/1.1 server on 127.0.0.1 for tests (java.net.ServerSocket only).
 * The android unit-test compile classpath has no com.sun.net.httpserver, and we don't
 * want mockwebserver as a new dependency.
 */
class TinyHttpServer(private val handler: (Request) -> Response) {
    data class Request(val path: String, val query: String?, val headers: Map<String, String>)
    class Response(val status: Int, val body: ByteArray, val contentType: String = "application/json")

    private val socket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
    val port: Int get() = socket.localPort

    @Volatile private var running = true

    init {
        thread(isDaemon = true, name = "tiny-http") {
            while (running) {
                val client = try { socket.accept() } catch (e: Exception) { break }
                thread(isDaemon = true) { serve(client) }
            }
        }
    }

    private fun serve(client: Socket) = client.use { c ->
        val reader = BufferedReader(InputStreamReader(c.getInputStream(), Charsets.ISO_8859_1))
        val requestLine = reader.readLine() ?: return
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
            val i = line.indexOf(':')
            if (i > 0) headers[line.substring(0, i).trim().lowercase()] = line.substring(i + 1).trim()
        }
        val target = requestLine.split(" ")[1]
        val qi = target.indexOf('?')
        val path = if (qi >= 0) target.substring(0, qi) else target
        val query = if (qi >= 0) target.substring(qi + 1) else null
        val res = handler(Request(path, query, headers))
        val reason = if (res.status == 200) "OK" else "ERR"
        val out = c.getOutputStream()
        out.write(
            ("HTTP/1.1 ${res.status} $reason\r\nContent-Type: ${res.contentType}\r\n" +
                "Content-Length: ${res.body.size}\r\nConnection: close\r\n\r\n").toByteArray(Charsets.ISO_8859_1)
        )
        out.write(res.body)
        out.flush()
    }

    fun stop() {
        running = false
        socket.close()
    }

}
