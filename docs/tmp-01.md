# h2 doc

官方文档
http://www.h2database.com/html/tutorial.html
http://www.h2database.com/html/quickstart.html

```text
Connecting to the Server using a Browser
If the server started successfully, you can connect to it using a web browser. 
If you started the server on the same computer as the browser, open the URL http://localhost:8082. 
If you want to connect to the application from another computer, you need to provide the IP address of the server, for example: http://192.168.0.2:8082. 
If you enabled TLS on the server side, the URL needs to start with https://.



Login
At the login page, you need to provide connection information to connect to a database. 
Set the JDBC driver class of your database, the JDBC URL, user name, and password. If you are done, click [Connect].
You can save and reuse previously saved settings. The settings are stored in a properties file (see Settings of the H2 Console).
```

h2-<version>.jar包含main类,支持java -jar启动方式.
```text
java -cp h2-<version>.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists -trace

解释:
-tcp: Enables the TCP server.
-tcpAllowOthers: Allows connections from remote hosts (not just localhost).
-ifNotExists: Databases are created when accessed
-trace: Print additional trace information (all servers)
org.h2.tools.Server是h2的启动类,如果需要其他flag option,可以查看org.h2.tools.Server
```


org.h2.tools.Server是h2服务启动类
org.h2.server.Service是h2服务(tcp/web/pg)具体的实现类
```text
public Server(Service service, String... args) throws SQLException {
    verifyArgs(args);
    this.service = service;
    try {
        service.init(args);
    } catch (Exception e) {
        throw DbException.toSQLException(e);
    }
}
```

org.h2.server.Service的当前实现有:
org.h2.server.TcpServer
org.h2.server.web.WebServer
org.h2.server.pg.PgServer


;TRACE_LEVEL_FILE=3;TRACE_LEVEL_SYSTEM_OUT=3

## 日志
通过jdbc-url配置日志级别.
```text
在H2的JDBC URL中添加TRACE_LEVEL_FILE和TRACE_LEVEL_SYSTEM_OUT参数，可以控制日志的输出。
TRACE_LEVEL_FILE：设置日志级别并指定日志文件的输出。
TRACE_LEVEL_SYSTEM_OUT：设置日志级别并输出到控制台。

日志级别对应关系如下：
0：关闭日志（OFF）
1：错误（ERROR）
2：警告（WARN）
3：信息（INFO）
4：调试（DEBUG）

示例:
jdbc:h2:tcp://192.168.1.2:9092/~/test;TRACE_LEVEL_FILE=3;TRACE_LEVEL_SYSTEM_OUT=3
jdbc:h2:mem:testdb;TRACE_LEVEL_FILE=3;TRACE_LEVEL_SYSTEM_OUT=3

```

## 持久化

```text
H2 can persist data to disk in server mode. 
In fact, this is one of the main use cases for running H2 in server mode. 
By default, when you run H2 in server mode, the database can be configured to use a file-based database that stores its data on disk rather than in memory.
```

### File-based Databases

You can configure H2 to store its data on disk by using a file-based connection URL. This way, the data is persisted across server restarts.

If you choose a file-based database, the data will be stored in a .mv.db file (H2 database file format).
This file will persist across server restarts.

Example of a file-based database URL:
```text
jdbc:h2:tcp://localhost:9092/~/mydb

~/mydb: This will create or use a file-based database named mydb in the home directory of the user running the H2 server
```

### Specify Database Location

You can specify the exact location of the database file

```text
jdbc:h2:tcp://localhost:9092/absolute/path/to/db/mydb
```

### In-memory Database

If you use an in-memory database (e.g., jdbc:h2:tcp://localhost:9092/mem:test),
the data will only persist for the duration of the server session, and it will be lost when the server is stopped.
