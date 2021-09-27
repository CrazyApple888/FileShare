import kotlinx.coroutines.*

fun main(args: Array<String>) {
    val port = if (args.isEmpty()) {
        println("Using default port 8080. If You want to use specified, restart program and pass port as argument")
        8080
    } else {
        args[0].toInt()
    }
    Server(port).start()
}