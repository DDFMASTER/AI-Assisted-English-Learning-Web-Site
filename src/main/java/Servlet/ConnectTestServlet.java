package Servlet;

import Utils.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/api/connect-test")
public class ConnectTestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        boolean dbConnected = false;
        String dbMessage = "";

        try (Connection conn = DBUtil.getConnection()) {
            dbConnected = conn != null && !conn.isClosed();
            dbMessage = dbConnected ? "数据库连接正常" : "数据库连接异常";
        } catch (Exception e) {
            dbMessage = "数据库连接失败: " + e.getMessage();
        }

        response.getWriter().write(
                "{\"message\":\"后端连接成功\""
                + ",\"dbConnected\":" + dbConnected
                + ",\"dbMessage\":\"" + escapeJson(dbMessage) + "\""
                + "}");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
