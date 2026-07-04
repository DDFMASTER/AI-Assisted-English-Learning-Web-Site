package Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 共享 Gson 实例工具。项目中所有 JSON 序列化统一使用此工具。
 */
public class GsonUtil {

    /** 紧凑模式（无缩进），用于 API 响应 */
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /** 美化模式，用于调试 */
    public static final Gson GSON_PRETTY = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    /**
     * 将对象序列化为 JSON 字符串（紧凑模式）。
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
}
