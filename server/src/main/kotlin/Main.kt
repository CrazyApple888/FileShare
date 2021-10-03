fun main(args: Array<String>) {
    val port = if (args.isEmpty()) {
        println("Using default port 8080. If You want to use specified, restart program and pass port as argument")
        8080
    } else {
        try {
            args[0].toInt()
        } catch (exc: NumberFormatException) {
            println("Invalid argument")
            return
        }
    }
    if (1023 >= port) {
        println("Invalid port")
        return
    }
    Server(port).start()
}