package org.example.learn.spring.boot.h2.server;

import org.example.learn.spring.boot.h2.server.util.JdbcUtils;
import org.h2.server.TcpServer;
import org.h2.tools.RunScript;
import org.h2.tools.Server;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * h2作为数据库服务器,不应该承担执行数据库脚本的任务.
 * 应该是数据库启动完成后,由单独的代码来执行
 */
public class H2TcpServerTest {

    private static final int PORT = 9092;

    @Test
    public void test0() throws Exception{
        // Start H2 in TCP Server mode
        TcpServer service = new TcpServer();
        Server h2TcpServer = new Server(service, "-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists", "-trace");
        service.setShutdownHandler(h2TcpServer);
        h2TcpServer.start();
        System.out.println("H2 Database Server started and running at: " + h2TcpServer.getURL());

        // 通过jdbc执行特定脚本
        runScript();
    }

    private void runScript() throws SQLException {
        String url = "jdbc:h2:tcp://localhost:9092/mem:test";

        // 执行初始化数据
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            // 从classpath中获取sql脚本内容
            InputStream inputStream = ClassLoader.getSystemResourceAsStream("init.sql");
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            RunScript.execute(conn, reader);
            System.out.println("SQL script executed successfully.");


            // 验证初始化数据(不能使用新的connection,否则就是一个空白的数据库)
            String sql = "select * from t_user";
            List<Map<String, Object>> result = JdbcUtils.executeQuery(conn, sql, null);
            System.out.println("result = " + result);
        }
    }
}
