package Servlet;

import Service.AIClient;
import Service.AIService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * AI 标题生成 — 根据文章内容生成英文标题
 *
 * POST /api/ai/generate-title
 * Body: content（文章全文）
 */
@WebServlet("/api/clickbait/generate-title")
public class AITitleServlet extends HttpServlet {
    private final AIClient aiClient = new AIClient();
    private final AIService aiService = new AIService(aiClient);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            String content = request.getParameter("content");
            if (content == null || content.trim().length() < 50) {
                response.getWriter().write(JsonUtil.error("文章内容过短，无法生成标题"));
                return;
            }

            String title = aiService.generateTitle(content.trim());
            if (title != null && !title.isBlank()) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"success\":true,");
                sb.append("\"title\":\"").append(escapeJson(title)).append("\"");
                sb.append("}");
                response.getWriter().write(sb.toString());
            } else {
                response.getWriter().write(JsonUtil.error("AI 标题生成失败，请稍后重试"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write(JsonUtil.error("服务器错误: " + e.getMessage()));
        }
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
