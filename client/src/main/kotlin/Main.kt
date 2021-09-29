import java.io.File
import java.net.InetAddress
import java.util.concurrent.Executors

// filename address port
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Pass filename or path, server address and port please")
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
    }


    Client(InetAddress.getByName("localhost"), 8080).start(file)

    /*val threadPool = Executors.newCachedThreadPool()
    for (i in 0..5) {
        threadPool.submit {
            Client(InetAddress.getByName("localhost"), 8080).start(file)
        }

        val file2 = File("../../raschetka.py")
        threadPool.submit {
            Client(InetAddress.getByName("localhost"), 8080).start(file2)
        }

        val file3 = File("../../main.cpp")
        threadPool.submit {
            Client(InetAddress.getByName("localhost"), 8080).start(file3)
        }
    }*/
}