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
 * 单词接龙接口。
 * POST /api/wordchain
 * Body: { "action": "start|validate|generate", "letter": "当前字母", "userWord": "用户单词", "usedWords": "已用单词逗号分隔" }
 */
@WebServlet("/api/wordchain")
public class WordChainServlet extends HttpServlet {

    private final AIClient aiClient = new AIClient();
    private final AIService aiService = new AIService(aiClient);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String body = readBody(request);
        String action = extractField(body, "action");
        String letter = extractField(body, "letter");
        String userWord = extractField(body, "userWord");
        String usedWords = extractField(body, "usedWords");

        if (action == null) action = "validate";
        if (letter == null) letter = "";
        if (userWord == null) userWord = "";
        if (usedWords == null) usedWords = "";

        String task;
        switch (action) {
            case "start" -> task = "start";
            case "generate" -> task = "generate";
            default -> task = "validate";
        }

        AIService.WordChainResult result = aiService.wordChain(task, letter, userWord, usedWords);
        response.getWriter().write(result.toJson());
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String extractField(String body, String fieldName) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":\"",
                "\"" + fieldName + "\": \"",
                "\"" + fieldName + "\" :\""
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                StringBuilder val = new StringBuilder();
                for (int i = start; i < body.length(); i++) {
                    char c = body.charAt(i);
                    if (c == '\\' && i + 1 < body.length()) {
                        char next = body.charAt(i + 1);
                        switch (next) {
                            case '"' -> { val.append('"'); i++; }
                            case '\\' -> { val.append('\\'); i++; }
                            case 'n' -> { val.append('\n'); i++; }
                            case 'r' -> { val.append('\r'); i++; }
                            case 't' -> { val.append('\t'); i++; }
                            default -> val.append(c);
                        }
                    } else if (c == '"') {
                        break;
                    } else {
                        val.append(c);
                    }
                }
                return val.toString();
            }
        }
        return null;
    }
}