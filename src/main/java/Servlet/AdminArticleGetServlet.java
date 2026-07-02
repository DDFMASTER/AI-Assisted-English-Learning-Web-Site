package Servlet;

import DAO.ArticleDAO;
import DAO.ArticleDAOImpl;
import Entities.Article;
import Service.AdminService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 管理员 — 获取单篇文章详情（含 content，用于编辑）
 *
 * GET /api/admin/article/get?id=xxx&adminUserId=xxx
 */
@WebServlet("/api/admin/article/get")
public class AdminArticleGetServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final ArticleDAO articleDAO = new ArticleDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long adminUserId = parseLong(request.getParameter("adminUserId"));
            if (!adminService.isAdmin(adminUserId)) {
                response.getWriter().write(JsonUtil.error("无管理员权限"));
                return;
            }

            Long articleId = parseLong(request.getParameter("id"));
            if (articleId == null) {
                response.getWriter().write(JsonUtil.error("缺少文章ID"));
                return;
            }

            Article article = articleDAO.findById(articleId);
            if (article == null) {
                response.getWriter().write(JsonUtil.error("文章不存在"));
                return;
            }

            // 返回文章完整信息
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":true,");
            sb.append("\"article\":{");
            sb.append("\"articleId\":").append(article.getArticleId()).append(",");
            sb.append("\"title\":\"").append(escapeJson(article.getTitle())).append("\",");
            sb.append("\"content\":\"").append(escapeJson(article.getContent())).append("\",");
            sb.append("\"source\":\"").append(escapeJson(article.getSource())).append("\",");
            sb.append("\"difficulty\":\"").append(escapeJson(article.getDifficulty())).append("\"");
            sb.append("}}");
            response.getWriter().write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write(JsonUtil.error("服务器错误: " + e.getMessage()));
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
