package Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet 公共工具 — 提供 parseLong、管理员认证等所有 Servlet 复用的方法。
 */
public class ServletUtil {

    /**
     * 安全解析 Long 类型请求参数。
     * @return 解析结果，若参数为 null、空字符串或非数字则返回 null
     */
    public static Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全解析 int 类型请求参数，解析失败返回默认值。
     */
    public static int parseInt(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 认证管理员身份 — 从 session 获取当前登录用户 ID 并校验是否为 admin。
     * 若认证失败，自动向 response 写入错误 JSON 并返回 null。
     * 若认证成功，返回管理员 user_id（Long）。
     */
    public static Long authenticateAdmin(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Service.AdminService adminService)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtil.error("请先登录"));
            return null;
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtil.error("请先登录"));
            return null;
        }

        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtil.error("无管理员权限"));
            return null;
        }

        return userId;
    }

    /**
     * 从 session 获取当前登录用户 ID。未登录返回 null。
     */
    public static Long getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Long) session.getAttribute("userId");
    }
}
