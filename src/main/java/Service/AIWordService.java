package Service;

import Utils.ConfigUtil;
import Utils.GsonUtil;
import Utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 单词服务 — 单词例句生成、AI 查词。
 */
public class AIWordService {

    private static final String SYSTEM_PROMPT =
            ConfigUtil.readResourceText("prompts/word-examples.txt");

    private static final String WORD_LOOKUP_PROMPT =
            ConfigUtil.readResourceText("prompts/word-lookup.txt");

    private static final int TIMEOUT_SECONDS = ConfigUtil.getInt("ai.api.timeout_seconds", 15);

    private final AIClient client;

    public AIWordService(AIClient client) {
        this.client = client;
    }

    /** 为指定单词生成 3 个例句 */
    public AIExampleResult generateExamples(String word) {
        AIExampleResult result = new AIExampleResult();
        result.word = word;
        long startTime = System.currentTimeMillis();

        String content = client.call(SYSTEM_PROMPT, word, TIMEOUT_SECONDS, result);

        if (content != null) {
            parseExamples(content, result);

            if (result.examples == null || result.examples.length == 0) {
                result.error = "AI 例句解析失败";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    /** AI 查词：当所有词书和 ai_word_dic 都未命中时调用 */
    public AiWordLookupResult lookupWordByAI(String word) {
        AiWordLookupResult result = new AiWordLookupResult();
        result.word = word;
        long startTime = System.currentTimeMillis();

        String content = client.call(WORD_LOOKUP_PROMPT, word, 20, new AIClient.AIResultBase());

        if (content != null) {
            for (String key : new String[]{"\"phonetic\":\"", "\"phonetic\": \"", "\"phonetic\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.phonetic = client.extractStringValue(content, start);
                    break;
                }
            }
            for (String key : new String[]{"\"translation\":\"", "\"translation\": \"", "\"translation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.translation = client.extractStringValue(content, start);
                    break;
                }
            }
            for (String key : new String[]{"\"explanation\":\"", "\"explanation\": \"", "\"explanation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.explanation = client.extractStringValue(content, start);
                    break;
                }
            }

            if (result.translation == null || result.translation.isEmpty()) {
                result.error = "AI 查词解析失败";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    private void parseExamples(String contentJson, AIExampleResult result) {
        List<String[]> examples = new ArrayList<>();
        String[] enKeys = {"\"en\":\"", "\"en\": \"", "\"en\" :\""};
        String[] zhKeys = {"\"zh\":\"", "\"zh\": \"", "\"zh\" :\""};

        int pos = 0;
        while (pos < contentJson.length() && examples.size() < 3) {
            int enStart = -1, enKeyLen = 0;
            for (String k : enKeys) {
                int idx = contentJson.indexOf(k, pos);
                if (idx != -1 && (enStart == -1 || idx < enStart)) { enStart = idx; enKeyLen = k.length(); }
            }
            if (enStart == -1) break;
            pos = enStart + enKeyLen;
            String en = client.extractStringValue(contentJson, pos);

            int zhStart = -1, zhKeyLen = 0;
            for (String k : zhKeys) {
                int idx = contentJson.indexOf(k, pos);
                if (idx != -1 && (zhStart == -1 || idx < zhStart)) { zhStart = idx; zhKeyLen = k.length(); }
            }
            if (zhStart == -1) break;
            pos = zhStart + zhKeyLen;
            String zh = client.extractStringValue(contentJson, pos);

            if (en != null && zh != null) examples.add(new String[]{en, zh});
        }

        result.examples = examples.toArray(new String[0][]);
    }

    // ==================== 结果类 ====================

    public static class AIExampleResult extends AIClient.AIResultBase {
        public String word;
        public String[][] examples;

        public boolean isSuccess() {
            return error == null && examples != null && examples.length > 0;
        }

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", isSuccess());
            map.put("word", word);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            if (examples != null) {
                var exList = new java.util.ArrayList<java.util.Map<String, String>>();
                for (String[] ex : examples) {
                    exList.add(java.util.Map.of("en", ex[0], "zh", ex[1]));
                }
                map.put("examples", exList);
            }
            if (rawResponse != null && !rawResponse.isEmpty()) {
                map.put("rawResponse", rawResponse.length() > 3000
                        ? rawResponse.substring(0, 3000) + "...[截断]" : rawResponse);
            }
            return GsonUtil.toJson(map);
        }
    }

    public static class AiWordLookupResult extends AIClient.AIResultBase {
        public String word;
        public String phonetic;
        public String translation;
        public String explanation;
    }
}
