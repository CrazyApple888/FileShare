import java.io.File
import java.nio.charset.Charset

class FileShareDatagram(
    val file: File
) {
    fun prepareMessage(): ByteArray =
        "${file.name.length} ${file.name} ${file.length()}\r\n".toByteArray(Charset.defaultCharset())

    companion object {
        fun checkPrepareMessage(message: String) : Boolean {
            val messageAttributes = message.split(" ")
            if (messageAttributes.size != 3) {
                return false
            }
            if (messageAttributes[1].length != messageAttributes[0].toInt()) {
                return false
            }

            //TODO check filename

            return true
        }

        fun parseFileName(prepareMessage: String) = prepareMessage.split(" ")[1]
    }
}