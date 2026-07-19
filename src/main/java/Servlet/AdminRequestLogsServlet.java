package Servlet;

import Entities.RequestLogEntry;
import Service.AdminService;
import Service.MonitorService;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 管理员 — 查阅/管理 IP 请求日志
 *
 * GET  /api/admin/monitor/request-logs?adminUserId=&page=1&pageSize=15
 * POST /api/admin/monitor/request-logs  body: { "action": "delete|clear|backup", "index": 5 }
 */
@WebServlet("/api/admin/monitor/request-logs")
public class AdminRequestLogsServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final MonitorService monitorService = new MonitorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long adminUserId = ServletUtil.authenticateAdmin(request, response, adminService);
        if (adminUserId == null) return;

        int page = ServletUtil.parseInt(request.getParameter("page"), 1);
        int pageSize = ServletUtil.parseInt(request.getParameter("pageSize"), 15);

        // 筛选参数
        String ipFilter = request.getParameter("ipFilter");
        String timeFrom = request.getParameter("timeFrom");
        String timeTo = request.getParameter("timeTo");
        String userFilter = request.getParameter("userFilter");

        ServletContext ctx = getServletContext();
        MonitorService.LogPageResult result;
        if ((ipFilter != null && !ipFilter.isBlank())
                || (timeFrom != null && !timeFrom.isBlank())
                || (timeTo != null && !timeTo.isBlank())
                || (userFilter != null && !userFilter.isBlank())) {
            result = monitorService.getFilteredLogsPage(ctx, page, pageSize,
                    ipFilter, timeFrom, timeTo, userFilter);
        } else {
            result = monitorService.getLogsPage(ctx, page, pageSize);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true");
        json.append(",\"totalCount\":").append(result.getTotal());
        json.append(",\"page\":").append(result.getPage());
        json.append(",\"totalPages\":").append(result.getTotalPages());
        json.append(",\"logs\":[");
        boolean first = true;
        for (RequestLogEntry entry : result.getItems()) {
            if (!first) json.append(",");
            first = false;
            json.append("{");
            json.append("\"ip\":").append(JsonUtil.strVal(entry.getIp())).append(",");
            json.append("\"path\":").append(JsonUtil.strVal(entry.getPath())).append(",");
            json.append("\"method\":").append(JsonUtil.strVal(entry.getMethod())).append(",");
            json.append("\"timestamp\":").append(JsonUtil.strVal(entry.getFormattedTime())).append(",");
            json.append("\"username\":").append(JsonUtil.strVal(entry.getUsername())).append(",");
            json.append("\"sessionId\":").append(JsonUtil.strVal(entry.getSessionId() != null ? entry.getSessionId().substring(0, Math.min(8, entry.getSessionId().length())) : ""));
            json.append("}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long adminUserId = ServletUtil.authenticateAdmin(request, response, adminService);
        if (adminUserId == null) return;

        String body = readBody(request);
        String action = extractField(body, "action");

        if (action == null) {
            response.getWriter().write(JsonUtil.error("缺少 action 参数"));
            return;
        }

        String result;
        switch (action) {
            case "delete" -> result = handleDelete(request, body, adminUserId);
            case "clear"  -> result = handleClear(request, adminUserId);
            case "backup" -> {
                handleBackup(response);
                return;
            }
            default -> result = JsonUtil.error("未知操作: " + action);
        }

        response.getWriter().write(result);
    }

    // ============================================
    // 删除单条日志
    // ============================================
    private String handleDelete(HttpServletRequest request, String body, Long adminUserId) {
        int index = extractIntField(body, "index", -1);
        if (index < 0) return JsonUtil.error("缺少有效的 index 参数");

        boolean ok = monitorService.deleteLog(getServletContext(), index);
        if (ok) {
            adminService.logAction(adminUserId, "monitor", null, "delete_log", "index=" + index);
            return "{\"success\":true,\"message\":\"已删除\"}";
        }
        return JsonUtil.error("删除失败，索引无效");
    }

    // ============================================
    // 清空全部日志
    // ============================================
    private String handleClear(HttpServletRequest request, Long adminUserId) {
        monitorService.clearLogs(getServletContext());
        adminService.logAction(adminUserId, "monitor", null, "clear_all_logs", "");
        return "{\"success\":true,\"message\":\"已清空全部日志\"}";
    }

    // ============================================
    // 备份（导出 TXT 文件下载）
    // ============================================
    private void handleBackup(HttpServletResponse response) throws IOException {
        List<RequestLogEntry> all = monitorService.getAllLogs(getServletContext());

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=AAEL-IP-logs-" + System.currentTimeMillis() + ".txt");

        PrintWriter pw = response.getWriter();
        pw.println("=== AAEL IP 请求日志备份 ===");
        pw.println("导出时间: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        pw.println("总条数: " + all.size());
        pw.println("===========================================");
        pw.println();

        for (RequestLogEntry e : all) {
            pw.printf("[%s] %s %s | IP: %s | 用户: %s | Session: %s%n",
                    e.getFormattedTime(),
                    e.getMethod(),
                    e.getPath(),
                    e.getIp(),
                    e.getUsername() != null ? e.getUsername() : "-",
                    e.getSessionId() != null ? e.getSessionId() : "-");
        }
        pw.flush();
    }

    // ============================================
    // JSON 辅助
    // ============================================
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

    private int extractIntField(String body, String fieldName, int defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                StringBuilder num = new StringBuilder();
                while (start < body.length() && Character.isDigit(body.charAt(start))) {
                    num.append(body.charAt(start));
                    start++;
                }
                if (num.length() > 0) {
                    try { return Integer.parseInt(num.toString()); }
                    catch (NumberFormatException e) { return defaultVal; }
                }
            }
        }
        return defaultVal;
    }
}
