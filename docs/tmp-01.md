# h2 server mode

h2除了可以以embedded-mode模式共同进程的java代码调用,还可以以server-mode模式供其他进程以jdbc的方式调用

h2-<version>.jar包含main类,支持java -jar启动方式.
```text
java -cp h2-<version>.jar org.h2.tools.Server -tcp -tcpAllowOthers

解释:
-tcp: Enables the TCP server.
-tcpAllowOthers: Allows connections from remote hosts (not just localhost).

org.h2.tools.Server是h2的启动类,如果需要其他flag option,可以查看org.h2.tools.Server
```
