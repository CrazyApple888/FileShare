import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket

class Client(
    address: InetAddress,
    port: Int
) {

    private val socket = Socket(address, port)

    fun start(file: File) {
        socket.getOutputStream().write(FileShareDatagram(file).prepareMessage())
        //val fis = DataInputStream(file.inputStream())//FileInputStream(file)
        val fis = FileInputStream(file)

        val buffer = ByteArray(512)
        while (!socket.isClosed) {
            val res = fis.read(buffer)
            if (-1 == res) {
                break
            }
            socket.getOutputStream().write(buffer, 0, res)
        }
        socket.getOutputStream().close()
        socket.close()
    }
}