import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket
import java.util.logging.Logger

class Client(
    private val address: InetAddress,
    private val port: Int
) {

    private val logger = Logger.getLogger(javaClass.name)
    private lateinit var socket: Socket

    fun start(file: File) {
        try {
            socket = Socket(address, port)
        } catch (_: Exception) {
            logger.warning("Can't open socket")
            return
        }

        val socketWriter = DataOutputStream(socket.getOutputStream())
        val prepareMessage = FileShareProtocol(file).prepareMessage()
        socketWriter.writeInt(prepareMessage.size)
        socketWriter.write(prepareMessage)
        val sendFileStream = FileInputStream(file)

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int
        while (true) {
            bytesRead = sendFileStream.read(buffer, 0, DEFAULT_BUFFER_SIZE)
            if (-1 == bytesRead) {
                break
            }
            socketWriter.write(buffer, 0, bytesRead)
        }

        val input = socket.getInputStream()
//        while (true) {
            bytesRead = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)
//            if (DEFAULT_BUFFER_SIZE != bytesRead) {
//                break
//            }
//        }
        println("${file.name} status: ${String(buffer, 0, bytesRead)}")
    }
}