package Utils;

/**
 * 通用 API 响应结果包装类。
 * 支持成功（带数据）和失败（带错误消息）两种状态。
 *
 * @param <T> 成功时携带的数据类型
 */
public class Result<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(true, "操作成功", null);
    }

    /** 成功（带消息） */
    public static <T> Result<T> ok(String message) {
        return new Result<>(true, message, null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(true, message, data);
    }

    /** 失败 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    /** 转为 JSON 字符串 */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":").append(success);
        sb.append(",\"message\":").append(JsonUtil.strVal(message));
        if (data != null) {
            // 如果 data 自身有 toJson，则用 toJson；否则用 toString
            sb.append(",\"data\":");
            try {
                var method = data.getClass().getMethod("toJson");
                sb.append(method.invoke(data));
            } catch (Exception e) {
                sb.append(JsonUtil.strVal(data.toString()));
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
