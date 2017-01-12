This is a test Cisco used to ask candidates applying for a developer's job in Cloud Web Security.
This test is not used anymore.

I have developed this using non-blocking IO, as I thought that using blocking IO was way too easy. Later when I was employed and was avaluating new candidates tests I have never seen anybody using non-blocking IO (it appears that I did more then required).

-------------------------------------------------

Original requirements:
- The candidate will have to write a simple Java server application that will support "telnet"-like connections. There is no need to implement the client side as well since the classic "telnet" application will be used for testing the server.
- The server must support multiple concurrent connections and it will have to respond to very basic commands like "ls", "cd", "mkdir", "pwd". It also must be portable across platforms (build time and run time).
- The use of the classes that invoke native commands (e.g. Runtime or ProcessBuilder) is not allowed.
- The candidate must also pay attention to code formatting, commenting and unit testing.
- The deliverable will be a project that can be built with Maven 2.
-------------------------------------------------

My original readme.txt:

This is the source code of a Test by Serguei Poliakov, created on 14/10/2012.
It is an implementation of a simple Java server application that suports "telnet"-like connections.

It was tested on Windows 7 (locally and via LAN) and on Mac (locally only)

Following commands are implemented:

ls, two options are implemented: -l and -1
cd
mkdir, no option
pwd, no options
quit - stops the connection

Class to execute: com.serguei.telnet.Telnet with one argument (optional, default 23) - port number.
