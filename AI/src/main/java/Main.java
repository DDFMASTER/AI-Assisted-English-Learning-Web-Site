import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;


import java.io.IOException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;


public class Main {

    // DeepSeek API 配置
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = "provide-ur-api-key-here"; // 请替换为你的 DeepSeek API Key
    private static final String MODEL = "deepseek-v4-flash";
    private static final int TIME_OUT = 30;

    // 系统提示词
    private static final String SYSTEM_PROMPT = """
            用户将向你提供一个英文单词。请以json形式输出该单词的简短翻译(100个字以内)以及详细解释(500个字以内)。注意在json中合理使用转义字符，换行应该用"\\n"表示。

            输入范例：
            hello

            输出的json范例：
            {
                "word": "hello",
                "meaning": "int./n.你好；(用于问候、接电话或引起注意)喂；(表示惊讶或认为别人说了蠢话或没有注意听)嘿",
                "explanation": "\\"hello\\" 是英语中最基础的问候语，中文翻译需根据语境灵活处理：\\n基本译法\\n"你好"（最通用，适用于大多数正式/非正式场合）\\n例：Hello, how are you? → 你好，最近怎么样？\\n口语化变体\\n"嗨"（更随意，常用于朋友间）\\n"喂"（接电话时使用，或引起注意）\\n例：Hello! Wait for me! → 喂！等等我！\\n特殊场景\\n"您好"（对长辈/客户表示尊敬）\\n"大家好"（面对群体时）\\n例：Hello everyone → 大家好\\n文化适配\\n中文问候常结合时间，如"早上好"对应"Good morning"，但"hello"本身无时间限制。在非正式网络交流中，也可直接用英文"hello"或缩写"hi"。"
            }
            """;

    // 用户提示词
    private static final String USER_PROMPT = "word";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // 构建消息列表
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", USER_PROMPT)
        );

        // 构建请求体（注意 response_format 必须是对象）
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", messages,
                "response_format", Map.of("type", "json_object")  // 关键修复
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        System.out.println("=== 请求体 ===");
        System.out.println(jsonBody);
        System.out.println();

        // 构建 HTTP 请求
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        // 创建自定义超时的 OkHttpClient（30 秒连接、写入、读取超时）
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(TIME_OUT))
                .writeTimeout(Duration.ofSeconds(TIME_OUT))
                .readTimeout(Duration.ofSeconds(TIME_OUT))
                .build();

        // 记录开始时间
        Instant start = Instant.now();
        System.out.println("=== 请求开始时间: " + start + " ===");

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);

            System.out.println("=== 响应状态码: " + response.code() + " ===");
            System.out.println("=== 响应耗时: " + duration.toSeconds() + " 秒 ===");
            System.out.println("=== 响应体 ===");
            // 格式化 JSON 输出
            try {
                // 解析为 JsonNode
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                // 使用 pretty printer 输出
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                System.out.println(prettyJson);
            } catch (Exception e) {
                // 若解析失败（例如响应不是 JSON），则直接输出原始内容
                System.out.println("（响应无法被解析为 JSON，输出原始内容）");
                System.out.println(responseBody);
            }
        }
    }
}
