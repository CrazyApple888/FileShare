import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket

class Client(
    private val address: InetAddress,
    private val port: Int
) {

    private lateinit var socket: Socket

    fun start(file: File) {
        try {
            socket = Socket(address, port)
        } catch (_: Exception) {
            println("Can't open socket")
            return
        }

        val writer = DataOutputStream(socket.getOutputStream())
        val prepareMessage = FileShareProtocol(file).prepareMessage()
        writer.writeInt(prepareMessage.size)
        writer.write(prepareMessage)
        val fis = FileInputStream(file)

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int
        while (true) {
            bytesRead = fis.read(buffer, 0, 10)
            if (-1 == bytesRead) {
                break
            }
            writer.write(buffer, 0, bytesRead)
        }

        val input = socket.getInputStream()
        while (true) {
            bytesRead = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)
            if (DEFAULT_BUFFER_SIZE != bytesRead) {
                break
            }
        }
        println("${file.name} status: ${String(buffer, 0, bytesRead)}")
    }
}