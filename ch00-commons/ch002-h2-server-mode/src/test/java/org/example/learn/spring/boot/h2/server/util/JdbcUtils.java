package org.example.learn.spring.boot.h2.server.util;

import java.sql.*;
import java.util.*;

public class JdbcUtils {

    /**
     * 将 ResultSet 转换为 List<Map<String, Object>> 形式
     *
     * @param rs 查询结果集
     * @return List<Map<String, Object>>
     * @throws SQLException SQL异常
     */
    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                rowMap.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            resultList.add(rowMap);
        }
        return resultList;
    }

    /**
     * 关闭 JDBC 资源 (ResultSet, Statement, Connection)
     *
     * @param rs  ResultSet
     * @param stmt Statement (PreparedStatement)
     * @param conn Connection
     */
    public static void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {}
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException ignored) {}
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {}
    }

    /**
     * 执行 SQL 查询并返回结果集
     *
     * @param conn  数据库连接
     * @param sql   查询 SQL 语句
     * @param params 查询参数（可选）
     * @return List<Map<String, Object>>
     * @throws SQLException SQL异常
     */
    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, Object... params) throws SQLException {
        List<Map<String, Object>> resultList;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                resultList = resultSetToList(rs);
            }
        }
        return resultList;
    }

    /**
     * 设置 PreparedStatement 参数
     *
     * @param pstmt  PreparedStatement
     * @param params 参数
     * @throws SQLException SQL异常
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
}
