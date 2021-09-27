import java.io.File
import java.net.InetAddress

// filename address port
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Pass filename or path, please")
        return
    } else if (args.size != 3) {
        println("Wrong arguments")
        println("Required: filename serverAddress port")
        return
    }
    val file = File(args[0])

    if (!file.exists()) {
        println("Wrong filename or path")
        return
    } else {
        println("${file.path}\n${file.name}\n${file.length()}")
    }

    Client(InetAddress.getByName("localhost"), 8080).start(file)
}