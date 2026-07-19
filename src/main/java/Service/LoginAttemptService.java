package Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 登录尝试限流服务 —— 追踪每个用户的登录失败次数。
 *
 * 规则：
 *   1. 同一用户名在 15 分钟内累计 10 次登录失败后，锁定 15 分钟
 *   2. 锁定期间所有登录尝试（无论密码正确与否）均被拒绝
 *   3. 成功登录或锁定过期后，失败计数自动清零
 *   4. 过期条目惰性清理，避免内存泄漏
 */
public class LoginAttemptService {

    /** 锁定阈值：失败次数达到此值后触发锁定 */
    private static final int MAX_ATTEMPTS = 10;

    /** 锁定持续时间（分钟） */
    private static final int LOCK_MINUTES = 15;

    /** 锁定持续时间（秒） */
    private static final long LOCK_SECONDS = TimeUnit.MINUTES.toSeconds(LOCK_MINUTES);

    /** 失败记录过期时间（分钟）：超过此时间未活动则清理 */
    private static final int EXPIRE_MINUTES = 30;

    /** 用户名 → 尝试记录 */
    private final ConcurrentHashMap<String, AttemptRecord> records = new ConcurrentHashMap<>();

    /**
     * 检查用户是否处于锁定状态。
     * @return true 如果当前被锁定
     */
    public boolean isLocked(String username) {
        if (username == null) return false;
        AttemptRecord record = records.get(username);
        if (record == null) return false;

        // 惰性清理已过期的锁定
        if (record.lockUntil > 0 && System.currentTimeMillis() > record.lockUntil) {
            records.remove(username);
            return false;
        }
        return record.lockUntil > 0 && System.currentTimeMillis() <= record.lockUntil;
    }

    /**
     * 获取锁定剩余秒数。
     * @return 剩余秒数，0 表示未锁定
     */
    public long getLockSecondsRemaining(String username) {
        if (username == null) return 0;
        AttemptRecord record = records.get(username);
        if (record == null || record.lockUntil <= 0) return 0;
        long remaining = (record.lockUntil - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * 获取剩余尝试次数。
     * @return 还允许失败几次（不含本次），0 表示下次失败即锁定
     */
    public int getRemainingAttempts(String username) {
        if (username == null) return MAX_ATTEMPTS;
        AttemptRecord record = records.get(username);
        if (record == null) return MAX_ATTEMPTS;

        // 惰性清理
        if (isExpired(record)) {
            records.remove(username);
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - record.failedAttempts);
    }

    /**
     * 记录一次登录失败。
     * @return 失败后剩余的尝试次数（0 表示已锁定）
     */
    public int recordFailure(String username) {
        if (username == null) return MAX_ATTEMPTS;

        AttemptRecord record = records.computeIfAbsent(username, k -> new AttemptRecord());

        // 如果旧记录已过期，重置
        if (isExpired(record)) {
            record.failedAttempts = 0;
            record.lockUntil = 0;
        }

        record.failedAttempts++;
        record.lastAttemptTime = System.currentTimeMillis();

        // 达到阈值：锁定
        if (record.failedAttempts >= MAX_ATTEMPTS) {
            record.lockUntil = System.currentTimeMillis() + LOCK_SECONDS * 1000;
        }

        return getRemainingAttempts(username);
    }

    /**
     * 登录成功后清除失败记录。
     */
    public void clearAttempts(String username) {
        if (username != null) {
            records.remove(username);
        }
    }

    /** 判断记录是否过期（超过 EXPIRE_MINUTES 未活动且未被锁定） */
    private boolean isExpired(AttemptRecord record) {
        if (record.lockUntil > 0 && System.currentTimeMillis() <= record.lockUntil) {
            return false; // 仍在锁定中，不过期
        }
        long expireMillis = TimeUnit.MINUTES.toMillis(EXPIRE_MINUTES);
        return (System.currentTimeMillis() - record.lastAttemptTime) > expireMillis;
    }

    /** 内部记录类 */
    private static class AttemptRecord {
        int failedAttempts = 0;
        long lastAttemptTime = System.currentTimeMillis();
        long lockUntil = 0; // 0 = 未锁定
    }
}
