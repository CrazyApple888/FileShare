import java.io.File
import java.net.InetAddress
import java.util.concurrent.Executors

// filename address port
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Pass filename or path, server address and port, please")
        return
    } else if (args.size != 3) {
        println("Wrong arguments")
        println("Required: filename serverAddress port")
        return
    }
    val file = File(args[0])
    val address = args[1]
    val port = try {
        args[0].toInt()
    } catch (exc: NumberFormatException) {
        println("Invalid port")
        return
    }

    if (!file.exists()) {
        println("Wrong filename or path")
        return
    }

    Client(InetAddress.getByName(address), port).start(file)
    //Comment this /\ and
    //Uncomment this \/ and write your files for easy stress test

    /*val threadPool = Executors.newCachedThreadPool()
    for (i in 0..5) {
        threadPool.execute {
            Client(InetAddress.getByName(address), port).start(file)
        }

        val file2 = File("../../raschetka.py")
        threadPool.execute {
            Client(InetAddress.getByName("localhost"), 8080).start(file2)
        }

        val file3 = File("../../main.cpp")
        threadPool.execute {
            Client(InetAddress.getByName("localhost"), 8080).start(file3)
        }

        val file4 = File("../../qwerty")
        threadPool.execute {
            Client(InetAddress.getByName("localhost"), 8080).start(file4)
        }
    }
    threadPool.shutdown()*/
}