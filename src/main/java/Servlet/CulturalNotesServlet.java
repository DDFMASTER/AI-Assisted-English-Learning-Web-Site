package Servlet;

import Service.AIClient;
import Service.AIService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 文章文化背景分析接口。
 * POST /api/article/cultural-notes
 * Body: { "content": "文章内容..." }
 * 返回 AI 识别出的文化背景知识点列表。
 */
@WebServlet("/api/article/cultural-notes")
public class CulturalNotesServlet extends HttpServlet {

    private final AIClient aiClient = new AIClient();
    private final AIService aiService = new AIService(aiClient);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String content = readContent(request);

        if (content == null || content.isBlank()) {
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"文章内容不能为空\"}");
            return;
        }

        AIService.CulturalNotesResult result = aiService.analyzeCulture(content);
        response.getWriter().write(result.toJson());
    }

    /** 从 POST body 中提取 content 字段的值 */
    private String readContent(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        // 简单的 JSON 解析：提取 "content" 字段
        for (String key : new String[]{"\"content\":\"", "\"content\": \"", "\"content\" :\""}) {
            int start = body.indexOf(key);
            if (start != -1) {
                start += key.length();
                StringBuilder val = new StringBuilder();
                for (int i = start; i < body.length(); i++) {
                    char c = body.charAt(i);
                    if (c == '\\' && i + 1 < body.length()) {
                        char next = body.charAt(i + 1);
                        val.append(c);
                        val.append(next);
                        i++;
                    } else if (c == '"') {
                        break;
                    } else {
                        val.append(c);
                    }
                }
                return unescapeJson(val.toString());
            }
        }
        return body; // fallback: 整个 body 当作内容
    }

    private String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
