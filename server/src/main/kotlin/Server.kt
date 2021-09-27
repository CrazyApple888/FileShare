import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class Server(port: Int) {
    private val serverSocket = ServerSocket(port)
    private val BUFSIZ = 200

    fun start() {
        while (!serverSocket.isClosed) {
            val client = serverSocket.accept()
            GlobalScope.launch(Dispatchers.IO) {
                downloadFile(client)
            }
        }
    }



    private fun downloadFile(source: Socket) {
        val reader = BufferedReader(InputStreamReader(source.getInputStream()))
        val msg = reader.readLine()

        if (FileShareDatagram.checkPrepareMessage(msg)) {
            println("AB")
        } else {
            println("HUI")
        }

        createUploads()

        val fileName = FileShareDatagram.parseFileName(msg)
        val file = File("uploads/$fileName")
        //file.createNewFile()

        //val fos = DataOutputStream(file.outputStream())
        val fos = FileOutputStream(file)

        val input = source.getInputStream()
        val buffer = ByteArray(BUFSIZ)
        while (!source.isClosed) {
            val result = input.read(buffer, 0, BUFSIZ)
            if (result == -1) {
                break
            }
            fos.write(buffer)
        }
        fos.close()
        input.close()
        source.close()

        println("File $fileName has been downloaded!")
    }
    
    private fun createUploads() {
        if (File("uploads").exists()) {
            return
        }
        File("uploads").mkdir()
    }

}