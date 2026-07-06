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
 * 角色扮演 AI 对话接口。
 * POST /api/roleplay/chat
 * Body: { "scene": "shopping|travel|ordering|interview", "role": "当前AI角色名", "message": "用户输入", "history": "历史对话摘要" }
 * 返回 AI 生成的旁白、角色对话和建议。
 */
@WebServlet("/api/roleplay/chat")
public class RolePlayServlet extends HttpServlet {

    private final AIClient aiClient = new AIClient();
    private final AIService aiService = new AIService(aiClient);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String body = readBody(request);

        String scene = extractField(body, "scene");
        String role = extractField(body, "role");
        String message = extractField(body, "message");
        String history = extractField(body, "history");

        if (scene == null || scene.isBlank()) {
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"场景不能为空\"}");
            return;
        }
        if (message == null) message = "";
        if (role == null || role.isBlank()) role = getDefaultRole(scene);
        if (history == null) history = "";

        AIService.RolePlayResult result = aiService.rolePlayChat(scene, role, message, history);
        response.getWriter().write(result.toJson());
    }

    private String getDefaultRole(String scene) {
        return switch (scene) {
            case "shopping" -> "Shop Assistant";
            case "travel" -> "Hotel Receptionist";
            case "ordering" -> "Waiter";
            case "interview" -> "HR Manager";
            default -> "Assistant";
        };
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** 从 JSON body 中提取指定字段的值 */
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
                            case '/' -> { val.append('/'); i++; }
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