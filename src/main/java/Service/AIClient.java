package Service;

import Utils.ConfigUtil;
import Utils.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AI API 通信客户端 — 封装 DeepSeek API 调用、JSON 解析、转义等底层逻辑。
 * 供 AIWordService、AIQuizService、AIService 等上层服务共用。
 */
public class AIClient {

    private static final String API_URL = ConfigUtil.get("ai.api.url");
    private static final String API_KEY = ConfigUtil.get("ai.api.key");
    private static final String MODEL = ConfigUtil.get("ai.api.model", "deepseek-v4-flash");
    private static final int DEFAULT_TIMEOUT = ConfigUtil.getInt("ai.api.timeout_seconds", 15);

    private final HttpClient httpClient;

    public AIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                .build();
    }

    // ==================== 公共 API ====================

    /**
     * 调用 DeepSeek API，返回提取的 content 字符串。
     * @param systemPrompt  系统提示词
     * @param userContent   用户输入
     * @param timeoutSeconds 超时秒数
     * @param result        结果基类（用于填充 httpStatus / rawResponse / error）
     * @return AI 返回的 content，失败返回 null（同时 result.error 被设置）
     */
    public String call(String systemPrompt, String userContent,
                       int timeoutSeconds, AIResultBase result) {
        try {
            String requestBody = buildRequestBody(systemPrompt, userContent);

            System.out.println("[AIClient] 请求 URL: " + API_URL);
            System.out.println("[AIClient] 请求体(前300字符): "
                    + (requestBody.length() > 300
                        ? requestBody.substring(0, 300) + "..."
                        : requestBody));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            result.httpStatus = response.statusCode();
            System.out.println("[AIClient] 响应状态: " + response.statusCode());

            if (response.statusCode() != 200) {
                result.rawResponse = response.body();
                result.error = "AI 服务返回错误状态码: " + response.statusCode();
                return null;
            }

            String body = response.body();
            result.rawResponse = body;
            System.out.println("[AIClient] 响应体(前500字符): "
                    + (body.length() > 500 ? body.substring(0, 500) + "..." : body));

            String content = extractContent(body);
            if (content == null || content.isEmpty()) {
                result.error = "AI 服务返回内容为空";
                return null;
            }

            content = stripMarkdownFence(content);
            content = normalizeJson(content);

            System.out.println("[AIClient] 提取的 content(前300字符): "
                    + (content.length() > 300 ? content.substring(0, 300) + "..." : content));

            return content;

        } catch (Exception e) {
            result.error = "AI 调用失败: " + e.getMessage();
            System.out.println("[AIClient] 异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** 从 JSON 字符串中提取指定键的整数值 */
    public int extractInt(String content, String key) {
        String searchKey = "\"" + key + "\":";
        int pos = content.indexOf(searchKey);
        if (pos == -1) return 0;
        pos += searchKey.length();
        return parseIntFromContent(content, pos);
    }

    /** 从指定位置提取 JSON 字符串值 */
    public String extractStringValue(String json, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> { sb.append('\\'); sb.append(next); }
                }
                i++;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ==================== 私有方法 ====================

    private String buildRequestBody(String systemPrompt, String userContent) {
        String safeSystem = JsonUtil.escapeJson(systemPrompt);
        String safeUser = JsonUtil.escapeJson(userContent);

        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + safeSystem + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + safeUser + "\"}"
                + "],"
                + "\"response_format\":{\"type\":\"json_object\"}"
                + "}";
    }

    private String normalizeJson(String json) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
                sb.append(c);
            } else if (!inString && (c == ' ' || c == '\n' || c == '\r' || c == '\t')) {
                // skip
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String stripMarkdownFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int endOfFirstLine = trimmed.indexOf('\n');
            if (endOfFirstLine != -1) {
                trimmed = trimmed.substring(endOfFirstLine + 1);
            }
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    private String extractContent(String responseBody) {
        for (String key : new String[]{"\"content\":\"", "\"content\": \"", "\"content\" :\""}) {
            int start = responseBody.indexOf(key);
            if (start != -1) {
                start += key.length();
                StringBuilder content = new StringBuilder();
                for (int i = start; i < responseBody.length(); i++) {
                    char c = responseBody.charAt(i);
                    if (c == '\\' && i + 1 < responseBody.length()) {
                        content.append(c);
                        content.append(responseBody.charAt(i + 1));
                        i++;
                    } else if (c == '"') {
                        break;
                    } else {
                        content.append(c);
                    }
                }
                return unescapeJson(content.toString());
            }
        }
        return null;
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

    private int parseIntFromContent(String s, int pos) {
        int val = 0;
        while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
            val = val * 10 + (s.charAt(pos) - '0');
            pos++;
        }
        return val;
    }

    // ==================== AIResultBase ====================

    public static class AIResultBase {
        public String error;
        public int httpStatus;
        public long duration;
        public String rawResponse;
    }
}
