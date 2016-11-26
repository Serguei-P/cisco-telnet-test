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
