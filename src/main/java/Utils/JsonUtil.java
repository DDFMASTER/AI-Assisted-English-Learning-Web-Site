package Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单的 JSON 序列化工具，避免引入 Jackson/Gson 等重量级依赖。
 * 提供静态方法构建常见 JSON 响应字符串。
 */
public class JsonUtil {

    /**
     * 构建 {"success": true/false, ...} 格式的基础响应
     */
    public static String buildResponse(boolean success, String message) {
        return String.format("{\"success\":%s,\"message\":\"%s\"}",
                success, escapeJson(message));
    }

    /**
     * 构建带额外键值对的响应
     */
    public static String buildResponse(boolean success, String message, String extraJson) {
        return String.format("{\"success\":%s,\"message\":\"%s\",%s}",
                success, escapeJson(message), extraJson);
    }

    /**
     * 构建错误响应
     */
    public static String error(String message) {
        return buildResponse(false, message);
    }

    /**
     * 构建成功响应
     */
    public static String success(String message) {
        return buildResponse(true, message);
    }

    /**
     * JSON 字符串转义
     */
    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * LocalDateTime 转为 "yyyy-MM-dd HH:mm:ss" 字符串，null 返回 "null"
     */
    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "null";
        return "\"" + dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"";
    }

    /**
     * 字符串值包装为 JSON 字符串（加引号并转义），null 返回 "null"
     */
    public static String strVal(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    /**
     * Long 值转 JSON 数字，null 返回 "null"
     */
    public static String numVal(Long n) {
        return n == null ? "null" : n.toString();
    }

    /**
     * Integer 值转 JSON 数字，null 返回 "null"
     */
    public static String numVal(Integer n) {
        return n == null ? "null" : n.toString();
    }

    /**
     * Boolean 值转 JSON 布尔，null 返回 "null"
     */
    public static String boolVal(Boolean b) {
        return b == null ? "null" : b.toString();
    }
}
