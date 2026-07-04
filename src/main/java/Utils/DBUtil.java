package Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接工具 — 基于 HikariCP 连接池。
 */
public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    private static final String URL = ConfigUtil.get("db.url");
    private static final String USER = ConfigUtil.get("db.username");
    private static final String PASSWORD = ConfigUtil.get("db.password");

    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // 连接池参数
            config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.maximumPoolSize", 20));
            config.setMinimumIdle(ConfigUtil.getInt("db.pool.minimumIdle", 5));
            config.setIdleTimeout(ConfigUtil.getInt("db.pool.idleTimeout", 300000));
            config.setMaxLifetime(ConfigUtil.getInt("db.pool.maxLifetime", 1200000));
            config.setConnectionTimeout(10000);

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP 连接池初始化完成 — maxPoolSize={}, minIdle={}",
                    config.getMaximumPoolSize(), config.getMinimumIdle());
        } catch (Exception e) {
            logger.error("数据库连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    /**
     * 从连接池获取数据库连接。
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接池（应用关闭时调用）。
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP 连接池已关闭");
        }
    }
}
