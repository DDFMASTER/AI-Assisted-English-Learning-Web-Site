package Service;

import DAO.UserDAO;
import DAO.UserDAOImpl;
import Entities.RequestLogEntry;
import Entities.User;
import Listener.AppContextListener;
import Listener.RequestLogListener;
import jakarta.servlet.ServletContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控服务 —— 为管理员提供在线用户统计、请求日志查阅、强制登出等功能。
 */
public class MonitorService {

    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * 在线用户信息 VO
     */
    public static class OnlineUserInfo {
        private final Long userId;
        private final String username;
        private final String role;
        private final String studyPurpose;
        private final String sessionId;
        private final long loginTime;
        private final long sessionCreatedAt;
        private final boolean isTimeout;

        public OnlineUserInfo(Long userId, String username, String role,
                              String studyPurpose, String sessionId,
                              long loginTime, long sessionCreatedAt, boolean isTimeout) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.studyPurpose = studyPurpose;
            this.sessionId = sessionId;
            this.loginTime = loginTime;
            this.sessionCreatedAt = sessionCreatedAt;
            this.isTimeout = isTimeout;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getStudyPurpose() { return studyPurpose; }
        public String getSessionId() { return sessionId; }
        public long getLoginTime() { return loginTime; }
        public long getSessionCreatedAt() { return sessionCreatedAt; }
        public boolean isTimeout() { return isTimeout; }
    }

    /**
     * 获取当前在线用户列表
     */
    public List<OnlineUserInfo> getOnlineUsers(ServletContext ctx) {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, String> userSessionMap =
                (ConcurrentHashMap<Long, String>) ctx.getAttribute(AppContextListener.USER_SESSION_MAP);

        if (userSessionMap == null || userSessionMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<OnlineUserInfo> result = new ArrayList<>();

        for (Long userId : userSessionMap.keySet()) {
            User user = userDAO.findById(userId);
            if (user == null) continue;

            String sessionId = userSessionMap.get(userId);
            long loginTime = 0;
            long sessionCreatedAt = 0;
            boolean timeout = false;

            // 尝试获取会话详情
            if (sessionId != null) {
                try {
                    // 通过 sessionId 获取会话（需要遍历，Tomcat 不直接支持）
                    // 这里我们通过监听器记录的属性来获取
                } catch (Exception ignored) {}
            }

            result.add(new OnlineUserInfo(
                    userId,
                    user.getUsername(),
                    user.getRole(),
                    user.getStudyPurpose(),
                    sessionId != null ? sessionId : "未知",
                    loginTime,
                    sessionCreatedAt,
                    timeout
            ));
        }

        // 按 userId 排序
        result.sort(Comparator.comparing(OnlineUserInfo::getUserId));
        return result;
    }

    /**
     * 获取在线人数
     */
    public int getOnlineCount(ServletContext ctx) {
        AtomicInteger count = (AtomicInteger) ctx.getAttribute(AppContextListener.ONLINE_COUNT);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取请求日志（最近 N 条）
     */
    public List<RequestLogEntry> getRecentLogs(ServletContext ctx, int limit) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);

        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }

        synchronized (logs) {
            int size = logs.size();
            int fromIndex = Math.max(0, size - limit);
            // 返回副本，倒序（最新在前）
            List<RequestLogEntry> result = new ArrayList<>(logs.subList(fromIndex, size));
            Collections.reverse(result);
            return result;
        }
    }

    /**
     * 获取全部请求日志数量
     */
    public int getTotalLogCount(ServletContext ctx) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);
        return logs != null ? logs.size() : 0;
    }

    /**
     * 分页获取请求日志（最新在前）
     * @return LogPageResult 包含当前页日志和分页信息
     */
    public LogPageResult getLogsPage(ServletContext ctx, int page, int pageSize) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);

        if (logs == null || logs.isEmpty()) {
            return new LogPageResult(Collections.emptyList(), 0, 1, 1);
        }

        synchronized (logs) {
            int total = logs.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            // 倒序取（最新在前）
            int fromIndex = total - (page * pageSize);
            if (fromIndex < 0) fromIndex = 0;
            int toIndex = total - ((page - 1) * pageSize);

            List<RequestLogEntry> pageItems = new ArrayList<>(logs.subList(fromIndex, toIndex));
            Collections.reverse(pageItems);
            return new LogPageResult(pageItems, total, page, totalPages);
        }
    }

    /**
     * 删除指定索引的日志（0-based，从旧到新）
     * @return true 成功，false 索引无效
     */
    public boolean deleteLog(ServletContext ctx, int index) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);
        if (logs == null || index < 0 || index >= logs.size()) return false;
        synchronized (logs) {
            if (index < logs.size()) {
                logs.remove(index);
                return true;
            }
            return false;
        }
    }

    /**
     * 获取全部日志（用于备份导出）
     */
    public List<RequestLogEntry> getAllLogs(ServletContext ctx) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);
        if (logs == null || logs.isEmpty()) return Collections.emptyList();
        synchronized (logs) {
            List<RequestLogEntry> result = new ArrayList<>(logs);
            Collections.reverse(result);
            return result;
        }
    }

    public static class LogPageResult {
        private final List<RequestLogEntry> items;
        private final int total;
        private final int page;
        private final int totalPages;
        public LogPageResult(List<RequestLogEntry> items, int total, int page, int totalPages) {
            this.items = items; this.total = total; this.page = page; this.totalPages = totalPages;
        }
        public List<RequestLogEntry> getItems() { return items; }
        public int getTotal() { return total; }
        public int getPage() { return page; }
        public int getTotalPages() { return totalPages; }
    }

    /**
     * 强制用户下线（通过 userId）。
     * 将目标用户 ID 加入 kickedUserIds 集合，
     * 用户的下一次请求将被 LoginFilter 拦截并强制销毁会话。
     *
     * @return null 表示成功，否则返回错误消息
     */
    public String kickUser(ServletContext ctx, Long targetUserId, Long adminUserId) {
        if (targetUserId == null) {
            return "缺少目标用户ID";
        }
        if (targetUserId.equals(adminUserId)) {
            return "不能强制自己下线";
        }

        // 检查目标用户是否存在
        User target = userDAO.findById(targetUserId);
        if (target == null) {
            return "用户不存在";
        }

        // 检查目标用户是否为管理员（不能踢管理员）
        if ("admin".equals(target.getRole())) {
            return "不能强制管理员下线";
        }

        // 查找用户是否在线
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, String> userSessionMap =
                (ConcurrentHashMap<Long, String>) ctx.getAttribute(AppContextListener.USER_SESSION_MAP);

        if (userSessionMap == null || !userSessionMap.containsKey(targetUserId)) {
            return "该用户当前不在线";
        }

        // 将用户加入强制下线集合
        @SuppressWarnings("unchecked")
        Set<Long> kickedSet = (Set<Long>) ctx.getAttribute(AppContextListener.KICKED_USER_IDS);
        if (kickedSet != null) {
            kickedSet.add(targetUserId);
        }

        // 从在线用户映射中移除（等待 LoginFilter 处理实际会话销毁）
        userSessionMap.remove(targetUserId);

        System.out.println("[AAEL] 管理员强制用户下线: targetUserId=" + targetUserId
                + " | adminUserId=" + adminUserId);

        return null; // 成功
    }

    /**
     * 清空请求日志
     */
    public void clearLogs(ServletContext ctx) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);
        if (logs != null) {
            synchronized (logs) {
                logs.clear();
            }
        }
    }

    // ==================== 日志文件持久化 ====================

    /** 日志文件存放目录 key（可从 config.properties 覆盖） */
    private static final String LOG_FILE_KEY = "requestlog.file.path";

    /**
     * 获取日志文件绝对路径。
     * 若 config 中配置的是相对路径（如 logs/request-logs.json），则相对于 webapp 的上级目录解析；
     * 若为绝对路径则直接使用。
     */
    public java.nio.file.Path resolveLogFilePath(ServletContext ctx) {
        String configured = Utils.ConfigUtil.get(LOG_FILE_KEY, "logs/request-logs.json");
        java.nio.file.Path p = java.nio.file.Path.of(configured);
        if (p.isAbsolute()) return p;
        // 相对路径：相对于 webapp 根目录的父级（如 Tomcat 的 webapps/ 目录）
        try {
            String webappRoot = ctx.getRealPath("/");
            if (webappRoot != null) {
                return java.nio.file.Path.of(webappRoot).getParent().resolve(configured);
            }
        } catch (Exception ignored) {}
        // 最终回退：当前工作目录
        return java.nio.file.Path.of("").toAbsolutePath().resolve(configured);
    }

    /** 将当前内存中的日志保存为 JSON 文件（含每日轮转 + 过期清理） */
    public void saveLogsToFile(ServletContext ctx) {
        List<RequestLogEntry> logs = getAllLogs(ctx);
        if (logs.isEmpty()) {
            System.out.println("[AAEL] 日志保存跳过: 无日志条目");
            return;
        }
        try {
            java.nio.file.Path filePath = resolveLogFilePath(ctx);
            java.nio.file.Files.createDirectories(filePath.getParent());

            // ---- 每日轮转：若当前文件来自昨天或更早，归档并重命名 ----
            if (java.nio.file.Files.exists(filePath)) {
                try {
                    java.nio.file.attribute.FileTime lastMod =
                            java.nio.file.Files.getLastModifiedTime(filePath);
                    java.time.LocalDate fileDate = java.time.LocalDate.from(
                            lastMod.toInstant().atZone(java.time.ZoneId.systemDefault()));
                    java.time.LocalDate today = java.time.LocalDate.now();
                    if (fileDate.isBefore(today)) {
                        String baseName = filePath.getFileName().toString();
                        int dotIdx = baseName.lastIndexOf('.');
                        String nameWithoutExt = dotIdx > 0 ? baseName.substring(0, dotIdx) : baseName;
                        String ext = dotIdx > 0 ? baseName.substring(dotIdx) : ".json";
                        java.nio.file.Path archive = filePath.getParent().resolve(
                                nameWithoutExt + "-" + fileDate.toString() + ext);
                        java.nio.file.Files.move(filePath, archive,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("[AAEL] 日志轮转: " + archive.getFileName());
                    }
                } catch (Exception ignored) {
                    // 轮转失败不影响本次保存
                }
            }

            // ---- 写入当前日志 ----
            String json = Utils.GsonUtil.toPrettyJson(logs);
            java.nio.file.Files.writeString(filePath, json, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("[AAEL] 日志已保存: " + filePath.toAbsolutePath()
                    + " (" + logs.size() + " 条)");

            // ---- 清理过期归档文件 ----
            cleanupOldLogFiles(filePath);

        } catch (Exception e) {
            System.err.println("[AAEL] 保存日志文件失败: " + e.getMessage()
                    + " | 路径=" + resolveLogFilePath(ctx));
        }
    }

    /** 删除超过保留天数的归档日志文件 */
    private void cleanupOldLogFiles(java.nio.file.Path currentFile) {
        int maxDays = Utils.ConfigUtil.getInt("requestlog.retention.days", 7);
        if (maxDays <= 0) return; // 0 或负数表示不自动清理

        java.nio.file.Path dir = currentFile.getParent();
        String baseName = currentFile.getFileName().toString();
        int dotIdx = baseName.lastIndexOf('.');
        String prefix = (dotIdx > 0 ? baseName.substring(0, dotIdx) : baseName) + "-";
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(maxDays);

        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(dir)) {
            files.filter(f -> {
                String name = f.getFileName().toString();
                return name.startsWith(prefix) && name.endsWith(".json");
            }).forEach(f -> {
                try {
                    // 从文件名提取日期: request-logs-2026-07-18.json → 2026-07-18
                    String name = f.getFileName().toString();
                    String dateStr = name.substring(prefix.length(), name.length() - 5); // 去掉 .json
                    java.time.LocalDate fileDate = java.time.LocalDate.parse(dateStr);
                    if (fileDate.isBefore(cutoff)) {
                        java.nio.file.Files.delete(f);
                        System.out.println("[AAEL] 日志清理: 已删除过期文件 " + name);
                    }
                } catch (Exception ignored) {
                    // 无法解析日期或删除失败则跳过
                }
            });
        } catch (Exception ignored) {
            // 目录列表失败则跳过清理
        }
    }

    /** 从 JSON 文件加载历史日志到内存 */
    @SuppressWarnings("unchecked")
    public void loadLogsFromFile(ServletContext ctx) {
        try {
            java.nio.file.Path filePath = resolveLogFilePath(ctx);
            if (!java.nio.file.Files.exists(filePath)) return;
            String json = java.nio.file.Files.readString(filePath, java.nio.charset.StandardCharsets.UTF_8);
            RequestLogEntry[] arr = Utils.GsonUtil.fromPrettyJson(json, RequestLogEntry[].class);
            if (arr == null || arr.length == 0) return;

            List<RequestLogEntry> logs = RequestLogListener.getLogList(ctx);
            synchronized (logs) {
                // 仅加载内存中不存在的条目（去重：同一时间戳 + 同一路径 + 同一 IP 视为重复）
                java.util.Set<String> existing = new java.util.HashSet<>();
                for (RequestLogEntry e : logs) {
                    existing.add(e.getIp() + "|" + e.getPath() + "|" + e.getFormattedTime());
                }
                for (RequestLogEntry e : arr) {
                    String key = e.getIp() + "|" + e.getPath() + "|" + e.getFormattedTime();
                    if (!existing.contains(key)) {
                        logs.add(e);
                    }
                }
            }
            System.out.println("[AAEL] 从文件加载了 " + arr.length + " 条历史日志");
        } catch (Exception e) {
            System.err.println("[AAEL] 加载日志文件失败: " + e.getMessage());
        }
    }

    // ==================== 日志过滤 ====================

    /** 过滤后的分页日志结果（支持按 IP、时间范围、用户筛选） */
    public LogPageResult getFilteredLogsPage(ServletContext ctx, int page, int pageSize,
                                              String ipFilter, String timeFrom, String timeTo,
                                              String userFilter) {
        @SuppressWarnings("unchecked")
        List<RequestLogEntry> logs =
                (List<RequestLogEntry>) ctx.getAttribute(RequestLogListener.REQUEST_LOGS);

        if (logs == null || logs.isEmpty()) {
            return new LogPageResult(Collections.emptyList(), 0, 1, 1);
        }

        // 在副本上过滤
        List<RequestLogEntry> filtered;
        synchronized (logs) {
            filtered = new ArrayList<>(logs);
        }

        // 按 IP 筛选（子串匹配）
        if (ipFilter != null && !ipFilter.isBlank()) {
            String f = ipFilter.trim().toLowerCase();
            filtered.removeIf(e -> e.getIp() == null || !e.getIp().toLowerCase().contains(f));
        }

        // 按用户筛选（子串匹配）
        if (userFilter != null && !userFilter.isBlank()) {
            String f = userFilter.trim().toLowerCase();
            filtered.removeIf(e -> e.getUsername() == null || !e.getUsername().toLowerCase().contains(f));
        }

        // 按时间范围筛选（格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-ddTHH:mm:ss）
        if ((timeFrom != null && !timeFrom.isBlank()) || (timeTo != null && !timeTo.isBlank())) {
            try {
                java.time.LocalDateTime from = parseTimeFilter(timeFrom);
                java.time.LocalDateTime to = parseTimeFilter(timeTo);
                filtered.removeIf(e -> {
                    if (e.getTimestamp() == null) return true;
                    if (from != null && e.getTimestamp().isBefore(from)) return true;
                    if (to != null && e.getTimestamp().isAfter(to)) return true;
                    return false;
                });
            } catch (Exception ignored) {}
        }

        // 倒序后分页
        Collections.reverse(filtered);
        int total = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<RequestLogEntry> pageItems = filtered.subList(fromIndex, toIndex);

        return new LogPageResult(new ArrayList<>(pageItems), total, page, totalPages);
    }

    private static java.time.LocalDateTime parseTimeFilter(String s) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        // 支持 ISO 格式和空格分隔格式
        try { return java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); } catch (Exception ignored) {}
        try { return java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME); } catch (Exception ignored) {}
        try { return java.time.LocalDateTime.parse(s + "T00:00:00"); } catch (Exception ignored) {} // 仅日期
        return null;
    }
}