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

        /**
        * @return on success, returns file name. Otherwise - empty string
        */
        fun parseFileName(prepareMessage: String) = if (checkPrepareMessage(prepareMessage)) {
            prepareMessage.split(' ')[1]
        } else ""

        /**
         * @return on success, returns file size. Otherwise -1
         */
        fun parseFileSize(prepareMessage: String) = if (checkPrepareMessage(prepareMessage)) {
            prepareMessage.trim().split(' ')[2].toLong()
        } else -1

        private fun checkPrepareMessage(prepareMessage: String): Boolean {
            val messageAttributes = prepareMessage.split(" ")
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