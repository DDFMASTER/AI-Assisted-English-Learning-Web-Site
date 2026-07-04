package Servlet;

import Service.AIClient;
import Service.AIWordService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * AI 例句生成接口。
 * GET /api/word/ai-examples?word=xxx
 * 返回该单词的 AI 生成例句（英文 + 中文翻译）。
 */
@WebServlet("/api/word/ai-examples")
public class AIExampleServlet extends HttpServlet {

    private final AIClient aiClient = new AIClient();
    private final AIWordService aiWordService = new AIWordService(aiClient);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String word = request.getParameter("word");

        if (word == null || word.isBlank()) {
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"单词参数不能为空\"}");
            return;
        }

        AIWordService.AIExampleResult result = aiWordService.generateExamples(word.trim());
        response.getWriter().write(result.toJson());
    }
}
