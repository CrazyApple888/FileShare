import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

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
            createFile()
            val receiveFileStream = FileOutputStream(file!!)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            try {
                while (!source.isClosed) {
                    val bytesRead = reader.read(buffer, 0, DEFAULT_BUFFER_SIZE)
                    if (-1 == bytesRead) {
                        break
                    }
                    receiveFileStream.write(buffer, 0, bytesRead)
                }
            } catch (_: IOException) {
                //Timeout expired, download complete
            }
            if (source.isClosed) {
                isFinished = true
                println("File $fileName hasn't been downloaded. Socket closed")
                reader.close()
                receiveFileStream.close()
                return
            }
            val msg = if (isDownloadSuccessful()) {
                FileShareProtocol.SUCCESS_MESSAGE.toByteArray(Charset.defaultCharset())
            } else {
                FileShareProtocol.FAIL_MESSAGE.toByteArray(Charset.defaultCharset())
            }
            source.getOutputStream().write(msg, 0, msg.size)
            receiveFileStream.close()
            source.close()
            reader.close()

            isFinished = true
            println("File $fileName has been downloaded!")
        }

        private suspend fun observeDownloadSpeed() {
            var cycleCounter: Long = 0
            var speedBeforeMeasure: Long = 0
            var averageSpeed = 0.0
            while (true) {
                delay(delayMillis)
                if (null == file) {
                    continue
                }
                if (isFinished) {
                    break
                }
                val currentSpeed = (file!!.length().toDouble() - speedBeforeMeasure) / (delayMillis / 1000)
                cycleCounter++
                averageSpeed = (averageSpeed + currentSpeed) / cycleCounter
                println(
                    "Download speed for ${file!!.name}: $currentSpeed bytes/second\n" +
                            "Average download speed for ${file!!.name}: $averageSpeed bytes/second"
                )
                speedBeforeMeasure = file!!.length()
            }
        }

        private fun createFile() {
            if (!File("uploads/$fileName").createNewFile()) {
                val fileNameParts = fileName.split('.')
                var fileExtension = ""
                var localName = fileName
                if (fileNameParts.size > 1) {
                    fileExtension = '.' + fileNameParts.last()
                    localName = fileName.dropLast(fileExtension.length)
                }
                var index = 1
                while (!File("uploads/$localName($index)$fileExtension").createNewFile()) {
                    index++
                }
                fileName = "$localName($index)$fileExtension"
            }

            file = File("uploads/$fileName")
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
            with(prepareMessage) {
                fileName = FileShareProtocol.parseFileName(this)
                fileSize = FileShareProtocol.parseFileSize(this)
            }

            return fileName.isNotEmpty() && fileSize != 0L
        }

        private fun isDownloadSuccessful() = file?.length() == fileSize
    }
}