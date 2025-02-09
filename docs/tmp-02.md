# h2 server mode

h2除了可以以embedded-mode模式共同进程的java代码调用,还可以以server-mode模式供其他进程以jdbc的方式调用

## TcpServer
TcpServer类提供了与h2数据库交互的tcp端口;
当前tcp协议详情不清楚,需要通过编程的方式使用org.h2.Driver来(本地/远程)交互.


## WebServer
WebServer类提供了与h2数据库交互的网页界面.
当页面选择本地的jdbc-url时,连接的就是WebServer进程内的h2数据库.
当页面选择远程的jdbc-url时,连接的就是WebServer进程外的h2数据库.

```text
通过配置jdbc的url, h2-console可用连接remote-h2 server
jdbc:h2:tcp://192.168.1.2:9092/~/testdb
```




## WebServer的实现

用户登录时,为没有用户分配一个web的session,该session会持有与数据库通讯的Connection,后续需要执行用户sql时,就使用该Connection来操作数据库
```text
# org.h2.server.web.WebApp.login

private String login(NetworkConnectionInfo networkConnectionInfo) {
    String driver = attributes.getProperty("driver", "");
    String url = attributes.getProperty("url", "");
    String user = attributes.getProperty("user", "");
    String password = attributes.getProperty("password", "");
    session.put("autoCommit", "checked");
    session.put("autoComplete", "1");
    session.put("maxrows", "1000");
    boolean isH2 = url.startsWith("jdbc:h2:");
    try {
        Connection conn = server.getConnection(driver, url, user, password, (String) session.get("key"), networkConnectionInfo);
        session.setConnection(conn);
        session.put("url", url);
        session.put("user", user);
        session.remove("error");
        settingSave();
        return "frame.jsp";
    } catch (Exception e) {
        session.put("error", getLoginError(e, isH2));
        return "login.jsp";
    }
}

# org.h2.server.web.WebServer.getConnection
Connection getConnection(String driver, String databaseUrl, String user, String password, String userKey, NetworkConnectionInfo networkConnectionInfo) throws SQLException {
    driver = driver.trim();
    databaseUrl = databaseUrl.trim();
    Properties p = new Properties();
    p.setProperty("user", user.trim());
    // do not trim the password, otherwise an
    // encrypted H2 database with empty user password doesn't work
    p.setProperty("password", password);
    if (databaseUrl.startsWith("jdbc:h2:")) {
        if (!allowSecureCreation || key == null || !key.equals(userKey)) {
            if (ifExists) {
                databaseUrl += ";FORBID_CREATION=TRUE";
            }
        }
    }
    return JdbcUtils.getConnection(driver, databaseUrl, p, networkConnectionInfo);
}

# org.h2.Driver.connect
public Connection connect(String url, Properties info) throws SQLException {
    return new JdbcConnection(url, info); // 创建Connection
}


JdbcConnection实现了java.sql.Connection,内部使用了SessionRemote来实现socket的远程通讯

# org.h2.jdbc.JdbcConnection.JdbcConnection(org.h2.engine.ConnectionInfo, boolean)
public JdbcConnection(ConnectionInfo ci, boolean useBaseDir) throws SQLException {
    try {
        if (useBaseDir) {
            String baseDir = SysProperties.getBaseDir();
            if (baseDir != null) {
                ci.setBaseDir(baseDir);
            }
        }
        // this will return an embedded or server connection
        session = new SessionRemote(ci).connectEmbeddedOrServer(false);
        trace = session.getTrace();
        int id = getNextId(TraceObject.CONNECTION);
        setTrace(trace, TraceObject.CONNECTION, id);
        this.user = ci.getUserName();
        this.url = ci.getURL();
        scopeGeneratedKeys = ci.getProperty("SCOPE_GENERATED_KEYS", false);
        closeOld();
        watcher = CloseWatcher.register(this, session, keepOpenStackTrace);
    } catch (Exception e) {
        throw logAndConvert(e);
    }
}

# org.h2.engine.SessionRemote.connectEmbeddedOrServer
public SessionInterface connectEmbeddedOrServer(boolean openNew) {
    ConnectionInfo ci = connectionInfo;
    if (ci.isRemote()) {
        connectServer(ci);
        return this;
    }
    // ...
}

# org.h2.engine.SessionRemote.connectServer
private void connectServer(ConnectionInfo ci) {
    // ...
    for (String s : servers) {
        Transfer trans = initTransfer(ci, databaseName, s);
        transferList.add(trans);
    }
    // ...
}


SessionRemote通过socket写入请求协议数据,并监听响应协议数据

# org.h2.engine.SessionRemote.initTransfer
private Transfer initTransfer(ConnectionInfo ci, String db, String server) throws IOException {
    Socket socket = NetUtils.createSocket(server, Constants.DEFAULT_TCP_PORT, ci.isSSL());
    Transfer trans = new Transfer(this, socket);
    trans.setSSL(ci.isSSL());
    trans.init();
    trans.writeInt(Constants.TCP_PROTOCOL_VERSION_MIN_SUPPORTED);
    trans.writeInt(Constants.TCP_PROTOCOL_VERSION_MAX_SUPPORTED);
    trans.writeString(db);
    trans.writeString(ci.getOriginalURL());
    trans.writeString(ci.getUserName());
    trans.writeBytes(ci.getUserPasswordHash());
    trans.writeBytes(ci.getFilePasswordHash());
    String[] keys = ci.getKeys();
    trans.writeInt(keys.length);
    for (String key : keys) {
        trans.writeString(key).writeString(ci.getProperty(key));
    }
    try {
        done(trans);
        clientVersion = trans.readInt();
        trans.setVersion(clientVersion);
        if (clientVersion >= Constants.TCP_PROTOCOL_VERSION_14) {
            if (ci.getFileEncryptionKey() != null) {
                trans.writeBytes(ci.getFileEncryptionKey());
            }
        }
        trans.writeInt(SessionRemote.SESSION_SET_ID);
        trans.writeString(sessionId);
        done(trans);
        if (clientVersion >= Constants.TCP_PROTOCOL_VERSION_15) {
            autoCommit = trans.readBoolean();
        } else {
            autoCommit = true;
        }
        return trans;
    } catch (DbException e) {
        trans.close();
        throw e;
    }
}

```