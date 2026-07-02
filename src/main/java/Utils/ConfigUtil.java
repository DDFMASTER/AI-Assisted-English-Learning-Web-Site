package Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 配置工具类 — 加载 classpath 下的 config.properties 和文本资源文件。
 * 使用方式：
 *   String url = ConfigUtil.get("ai.api.url");
 *   String prompt = ConfigUtil.readResourceText("prompts/word-examples.txt");
 */
public class ConfigUtil {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigUtil.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                System.err.println("[ConfigUtil] 警告: config.properties 未找到，"
                        + "请将 config.example.properties 复制为 config.properties 并填入真实配置");
            } else {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                System.out.println("[ConfigUtil] 配置加载成功，共 " + props.size() + " 项");
            }
        } catch (IOException e) {
            System.err.println("[ConfigUtil] 加载 config.properties 失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置值。
     * @param key 配置键
     * @return 配置值，未找到返回 null
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

    /**
     * 获取配置值，未找到时返回默认值。
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /**
     * 获取 int 类型配置值。
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static int getInt(String key, int defaultValue) {
        String val = props.getProperty(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            System.err.println("[ConfigUtil] 配置项 " + key + " 不是合法整数: " + val);
            return defaultValue;
        }
    }

    /**
     * 读取 classpath 下的文本资源文件全部内容（UTF-8）。
     * @param path 相对于 resources 根目录的路径，如 "prompts/word-examples.txt"
     * @return 文件全部文本内容
     */
    public static String readResourceText(String path) {
        try (InputStream in = ConfigUtil.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("资源文件不存在: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取资源文件失败: " + path, e);
        }
    }
}
