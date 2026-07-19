package Utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 共享 Gson 实例工具。项目中所有 JSON 序列化统一使用此工具。
 */
public class GsonUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** LocalDateTime 适配器 */
    private static final TypeAdapter<LocalDateTime> LOCAL_DATE_TIME_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value != null ? value.format(DT_FMT) : null);
        }
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String s = in.nextString();
            return s != null ? LocalDateTime.parse(s, DT_FMT) : null;
        }
    };

    /** 紧凑模式（无缩进），用于 API 响应 */
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /** 美化模式，用于调试 / 文件持久化（支持 LocalDateTime） */
    public static final Gson GSON_PRETTY = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_ADAPTER)
            .create();

    /**
     * 将对象序列化为 JSON 字符串（紧凑模式）。
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * 将对象序列化为美化 JSON 字符串（用于文件持久化）。
     */
    public static String toPrettyJson(Object obj) {
        return GSON_PRETTY.toJson(obj);
    }

    /**
     * 从 JSON 字符串反序列化（美化解析器，支持 LocalDateTime）。
     */
    public static <T> T fromPrettyJson(String json, Class<T> type) {
        return GSON_PRETTY.fromJson(json, type);
    }
}
