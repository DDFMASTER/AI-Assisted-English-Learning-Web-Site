package Listener;

import Service.MonitorService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局上下文监听器 —— 负责 Web 应用启动时的初始化与关闭时的资源释放。
 *
 * 初始化时创建：
 *   - onlineCount     (AtomicInteger) 在线人数计数器
 *   - userSessionMap  (ConcurrentHashMap<Long, String>) 用户ID → 会话ID 映射
 *   - kickedUserIds   (ConcurrentHashMap.newKeySet<Long>()) 被强制下线的用户ID集合
 *   - appStartTime    (LocalDateTime) 应用启动时间
 *
 * 同时启动日志持久化定时器和历史日志加载。
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    /** ServletContext 中存储在线用户数的 key */
    public static final String ONLINE_COUNT = "onlineCount";

    /** ServletContext 中存储用户-会话映射的 key */
    public static final String USER_SESSION_MAP = "userSessionMap";

    /** ServletContext 中存储被强制下线用户 ID 集合的 key */
    public static final String KICKED_USER_IDS = "kickedUserIds";

    /** ServletContext 中存储应用启动时间的 key */
    public static final String APP_START_TIME = "appStartTime";

    /** ServletContext 中存储 PK 游戏房间的 key (ConcurrentHashMap<String, GameRoom>) */
    public static final String ROOMS_MAP = "pkRooms";

    /** 日志持久化定时器 */
    private ScheduledExecutorService logSaveScheduler;

    /** ServletContext 中存储应用启动时日志条目数的 key */
    private static final String LOG_COUNT_AT_START = "logCountAtStart";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // 初始化全局属性
        ctx.setAttribute(ONLINE_COUNT, new AtomicInteger(0));
        ctx.setAttribute(USER_SESSION_MAP, new ConcurrentHashMap<Long, String>());
        ctx.setAttribute(KICKED_USER_IDS, ConcurrentHashMap.newKeySet());
        ctx.setAttribute(ROOMS_MAP, new ConcurrentHashMap<String, Entities.GameRoom>());
        ctx.setAttribute(APP_START_TIME, LocalDateTime.now());

        // 设置会话超时时间
        int timeoutMinutes = Utils.ConfigUtil.getInt("session.timeout.minutes", 30);
        ctx.setSessionTimeout(timeoutMinutes);

        // 加载历史日志并记录启动时的条目数
        MonitorService monitorService = new MonitorService();
        try {
            monitorService.loadLogsFromFile(ctx);
        } catch (Exception e) {
            System.err.println("[AAEL] 加载历史日志时出错: " + e.getMessage());
        }
        int startCount = monitorService.getTotalLogCount(ctx);
        ctx.setAttribute(LOG_COUNT_AT_START, startCount);

        // 启动定期保存日志的定时器（从 config 读取间隔，默认 5 分钟）
        int saveIntervalMinutes = Utils.ConfigUtil.getInt("requestlog.save.interval.minutes", 5);
        logSaveScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AAEL-LogSaver");
            t.setDaemon(true);
            return t;
        });
        logSaveScheduler.scheduleWithFixedDelay(
                () -> {
                    try { monitorService.saveLogsToFile(ctx); }
                    catch (Exception e) { System.err.println("[AAEL] 定时保存日志失败: " + e.getMessage()); }
                },
                saveIntervalMinutes, saveIntervalMinutes, TimeUnit.MINUTES);

        System.out.println("[AAEL] ========================================");
        System.out.println("[AAEL]        应 用 启 动 成 功");
        System.out.println("[AAEL] 启动时间 : " + LocalDateTime.now());
        System.out.println("[AAEL] 上下文路径: " + ctx.getContextPath());
        System.out.println("[AAEL] 会话超时  : " + timeoutMinutes + " 分钟");
        System.out.println("[AAEL] 日志持久化: 每 " + saveIntervalMinutes + " 分钟");
        System.out.println("[AAEL] 历史条目数: " + startCount);
        System.out.println("[AAEL] 日志文件  : " + monitorService.resolveLogFilePath(ctx).toAbsolutePath());
        System.out.println("[AAEL] ========================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // 停止日志保存定时器
        if (logSaveScheduler != null) {
            logSaveScheduler.shutdown();
            try { logSaveScheduler.awaitTermination(10, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }

        // 最后一次保存日志
        MonitorService monitorService = new MonitorService();
        try {
            monitorService.saveLogsToFile(ctx);
        } catch (Exception e) {
            System.err.println("[AAEL] 关闭保存日志失败: " + e.getMessage());
        }

        // 统计本次运行期间新增的日志条目数
        int currentLogCount = monitorService.getTotalLogCount(ctx);
        Integer startCount = (Integer) ctx.getAttribute(LOG_COUNT_AT_START);
        int newEntries = (startCount != null) ? (currentLogCount - startCount) : 0;

        AtomicInteger count = (AtomicInteger) ctx.getAttribute(ONLINE_COUNT);
        int onlineUsers = count != null ? count.get() : 0;

        LocalDateTime startTime = (LocalDateTime) ctx.getAttribute(APP_START_TIME);

        System.out.println("[AAEL] ========================================");
        System.out.println("[AAEL]        应 用 正 在 关 闭");
        System.out.println("[AAEL] 关闭时间 : " + LocalDateTime.now());
        System.out.println("[AAEL] 启动时间 : " + startTime);
        System.out.println("[AAEL] 在线用户 : " + onlineUsers);
        System.out.println("[AAEL] 日志条目 : " + currentLogCount + " 条（本次新增 " + Math.max(0, newEntries) + " 条）");
        System.out.println("[AAEL] ========================================");

        // 清理全局属性
        ctx.removeAttribute(ONLINE_COUNT);
        ctx.removeAttribute(USER_SESSION_MAP);
        ctx.removeAttribute(KICKED_USER_IDS);
        ctx.removeAttribute(ROOMS_MAP);
        ctx.removeAttribute("requestLogs");
        ctx.removeAttribute(APP_START_TIME);
    }
}