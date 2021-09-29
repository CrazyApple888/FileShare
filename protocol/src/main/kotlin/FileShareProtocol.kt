import java.io.File
import java.nio.charset.Charset

class FileShareProtocol(
    private val file: File
) {
    fun prepareMessage(): ByteArray =
        "${file.name.length} ${file.name} ${file.length()}\r\n".toByteArray(Charset.defaultCharset())

    companion object {

        const val SUCCESS_MESSAGE = "SUCCESS\r\n"
        const val FAIL_MESSAGE = "FAIL\r\n"
        const val AFTER_SEND_TIMEOUT = 1000

        fun parseFileName(prepareMessage: String) = prepareMessage.split(' ')[1]

        fun parseFileSize(prepareMessage: String) = prepareMessage.trim().split(' ')[2].toLong()

        fun checkPrepareMessage(message: String) : Boolean {
            val messageAttributes = message.split(" ")
            if (messageAttributes.size != 3) {
                return false
            }
            if (messageAttributes[1].length != messageAttributes[0].toInt()) {
                return false
            }

            return true
        }
    }
}