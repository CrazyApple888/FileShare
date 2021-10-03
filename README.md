# FileShare
Simple client-server program for sharing files. Server can download several files at 
the same time.

Client can send file to server
## Stress test
1. In client comment try-catch block with Client(InetAddress.getByName(address), port).start(file)
2. Uncomment code below. Replace file names with your files.
3. Run server.main()
4. Run client.main()