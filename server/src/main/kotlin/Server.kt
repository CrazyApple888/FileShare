import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path

class Server(port: Int) {
    private val serverSocket = ServerSocket(port)

    fun start() {
        while (!serverSocket.isClosed) {
            val client = serverSocket.accept()
            println("Accepted new client!")
            DownloadTask(client).execute()
        }
    }

    private inner class DownloadTask(
        private val source: Socket
    ) {

        private val delayMillis: Long = 1000
        private var file: File? = null
        private var isFinished = false
        private var fileName = ""
        private var fileSize: Long = 0

        fun execute() {
            CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                launch { observeDownloadSpeed() }
                download()
            }
        }

        private fun download() {
            source.soTimeout = FileShareProtocol.AFTER_SEND_TIMEOUT
            val reader = DataInputStream(source.getInputStream())
            val prepareMessage = getPrepareMessage(reader)
            if (!parsePrepareMessage(prepareMessage)) {
                reader.close()
                isFinished = true
                println("Can't download file")
                return
            }
            createUploads()
            file = createFile(fileName)
            val fos = FileOutputStream(file!!)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            try {
                while (!source.isClosed) {
                    val bytesRead = reader.read(buffer, 0, DEFAULT_BUFFER_SIZE)
                    if (-1 == bytesRead) {
                        break
                    }
                    fos.write(buffer, 0, bytesRead)
                }
            } catch (_: IOException) {
                //Timeout expired, download complete
            }
            isFinished = true

            val msg = if (isDownloadSuccessful()) {
                FileShareProtocol.SUCCESS_MESSAGE.toByteArray(Charset.defaultCharset())
            } else {
                FileShareProtocol.FAIL_MESSAGE.toByteArray(Charset.defaultCharset())
            }
            source.getOutputStream().write(msg, 0, msg.size)
            fos.close()
            source.close()
            reader.close()

            println("File $fileName has been downloaded!")
        }

        private suspend fun observeDownloadSpeed() {
            var cycleCounter: Long = 0
            var speedBeforeMeasure: Long = 0
            var averageSpeed = 0.0
            do {
                delay(delayMillis)
                if (null == file) {
                    continue
                }
                val currentSpeed = (file!!.length().toDouble() - speedBeforeMeasure) / (delayMillis / 1000)
                cycleCounter++
                averageSpeed = (averageSpeed + currentSpeed) / cycleCounter
                println(
                    "Download speed for ${file!!.name}: $currentSpeed bytes/second\n" +
                            "Average download speed for ${file!!.name}: $averageSpeed bytes/second"
                )
                speedBeforeMeasure = file!!.length()
            } while (!isFinished)
        }

        private fun createFile(fileName: String): File {
            if (Files.exists(Path("uploads/$fileName"))) {
                val fileExtension = fileName.split('.').last()
                val localName = fileName.dropLast(fileExtension.length + 1)
                var index = 1
                while (Files.exists(Path("uploads/$localName$index.$fileExtension"))) {
                    index++
                }
                return File("uploads/$localName$index.$fileExtension")
            }

            return File("uploads/$fileName")
        }

        private fun getPrepareMessage(reader: DataInputStream): String {
            val msgSize = reader.readInt()
            val bytes = ByteArray(msgSize)
            var i = 0
            while (true) {
                val byte = reader.readByte()
                bytes[i] = byte
                i++
                if (byte.toInt().toChar() == '\n') {
                    break
                }
            }

            return String(bytes)
        }

        private fun createUploads() {
            if (File("uploads").exists()) {
                return
            }
            File("uploads").mkdir()
        }

        private fun parsePrepareMessage(prepareMessage: String): Boolean {
            if (FileShareProtocol.checkPrepareMessage(prepareMessage)) {
                with(prepareMessage) {
                    fileName = FileShareProtocol.parseFileName(this)
                    fileSize = FileShareProtocol.parseFileSize(this)
                }
                return true
            }

            return false
        }

        private fun isDownloadSuccessful() = file?.length() == fileSize
    }
}