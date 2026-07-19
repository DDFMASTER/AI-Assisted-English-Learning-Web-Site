package Service;

import Utils.ConfigUtil;
import Utils.GsonUtil;
import Utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 服务 — 翻译、文化分析、标题生成。
 * 通用 API 调用已下沉至 AIClient，单词和测验逻辑已拆分至 AIWordService / AIQuizService。
 */
public class AIService {

    private static final String TRANSLATION_PROMPT =
            ConfigUtil.readResourceText("prompts/translation.txt");

    private static final String CULTURE_PROMPT =
            ConfigUtil.readResourceText("prompts/culture-analysis.txt");

    private static final String TITLE_PROMPT =
            ConfigUtil.readResourceText("prompts/title-generation.txt");

    private static final String ROLEPLAY_PROMPT_TEMPLATE =
            ConfigUtil.readResourceText("prompts/roleplay.txt");

    private static final String WORDCHAIN_PROMPT_TEMPLATE =
            ConfigUtil.readResourceText("prompts/wordchain.txt");

    /** 单词接龙 AI 调用超时秒数（config.properties: wordchain.ai.timeout.seconds，默认 30） */
    private static final int WORDCHAIN_AI_TIMEOUT =
            Utils.ConfigUtil.getInt("wordchain.ai.timeout.seconds", 30);

    private final AIClient client;

    public AIService(AIClient client) {
        this.client = client;
    }

    // ==================== 段落翻译 ====================

    public TranslationResult translateParagraph(String paragraph) {
        TranslationResult result = new TranslationResult();
        result.originalText = paragraph;
        long startTime = System.currentTimeMillis();

        String content = client.call(TRANSLATION_PROMPT, paragraph, 20, result);

        if (content != null) {
            for (String key : new String[]{"\"translation\":\"", "\"translation\": \"", "\"translation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    String t = client.extractStringValue(content, start);
                    if (t != null && !t.isEmpty()) { result.translation = t; break; }
                }
            }
            if (result.translation == null) result.error = "翻译结果解析失败";
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    // ==================== 文化背景分析 ====================

    public CulturalNotesResult analyzeCulture(String articleContent) {
        CulturalNotesResult result = new CulturalNotesResult();
        long startTime = System.currentTimeMillis();

        String content = articleContent.length() > 8000
                ? articleContent.substring(0, 8000) : articleContent;

        String response = client.call(CULTURE_PROMPT, content, 25, result);

        if (response != null) {
            List<CulturalNote> notes = new ArrayList<>();
            int pos = 0;
            while ((pos = response.indexOf("\"title\":\"", pos)) != -1) {
                pos += 9;
                String title = client.extractStringValue(response, pos);
                pos = response.indexOf("\"content\":\"", pos);
                if (pos == -1) break;
                pos += 11;
                String noteContent = client.extractStringValue(response, pos);
                String zh = "";
                int zhPos = response.indexOf("\"zh\":\"", pos);
                if (zhPos != -1) {
                    zhPos += 6;
                    zh = client.extractStringValue(response, zhPos);
                    if (zh == null) zh = "";
                }
                if (title != null && noteContent != null) {
                    notes.add(new CulturalNote(title, noteContent, zh));
                }
            }
            result.notes = notes.toArray(new CulturalNote[0]);
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    // ==================== AI 标题生成 ====================

    public String generateTitle(String articleContent) {
        AIClient.AIResultBase r = new AIClient.AIResultBase();
        String content = client.call(TITLE_PROMPT, articleContent, 15, r);

        if (content != null) {
            for (String key : new String[]{"\"title\":\"", "\"title\": \"", "\"title\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    String title = client.extractStringValue(content, start);
                    if (title != null && !title.isBlank()) return title.trim();
                }
            }
        }
        return null;
    }

    // ==================== 单词接龙 ====================

    public WordChainResult wordChain(String task, String letter, String userWord, String usedWords) {
        WordChainResult result = new WordChainResult();
        long startTime = System.currentTimeMillis();

        String prompt = WORDCHAIN_PROMPT_TEMPLATE
                .replace("{task}", task)
                .replace("{letter}", letter)
                .replace("{userWord}", userWord)
                .replace("{usedWords}", usedWords);

        String response = client.call(prompt, "[Go]", WORDCHAIN_AI_TIMEOUT, result);

        if (response != null) {
            result.valid = "true".equals(extractSimpleField(response, "valid"));
            result.word = extractJsonField(response, "word");
            result.reason = extractJsonField(response, "reason");
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    private String extractSimpleField(String json, String fieldName) {
        // 提取布尔或字符串值，不带引号处理
        for (String key : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
        }) {
            int start = json.indexOf(key);
            if (start != -1) {
                start += key.length();
                while (start < json.length() && json.charAt(start) == ' ') start++;
                if (start < json.length() && json.charAt(start) == '"') {
                    return client.extractStringValue(json, start + 1);
                }
                // 布尔值
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != '\n') end++;
                return json.substring(start, end).trim();
            }
        }
        return null;
    }

    // ==================== 角色扮演对话 ====================

    public RolePlayResult rolePlayChat(String scene, String role, String message, String history) {
        RolePlayResult result = new RolePlayResult();
        long startTime = System.currentTimeMillis();

        String prompt = ROLEPLAY_PROMPT_TEMPLATE
                .replace("{scene}", scene)
                .replace("{role}", role)
                .replace("{history}", history != null ? history : "")
                .replace("{userMessage}", message != null ? message : "");

        String response = client.call(prompt, "[Start conversation]", 30, result);

        if (response != null) {
            // 解析 narrator
            result.narratorEn = extractJsonField(response, "en");
            result.narratorZh = extractJsonField(response, "zh");
            // 解析 character
            result.characterName = extractJsonField(response, "name");
            result.characterDialogue = extractJsonField(response, "dialogue");
            // 解析 suggestion
            result.suggestion = extractJsonField(response, "suggestion");
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    private String extractJsonField(String json, String fieldName) {
        for (String key : new String[]{
                "\"" + fieldName + "\":\"",
                "\"" + fieldName + "\": \"",
                "\"" + fieldName + "\" :\""
        }) {
            int start = json.indexOf(key);
            if (start != -1) {
                start += key.length();
                return client.extractStringValue(json, start);
            }
        }
        return null;
    }

    // ==================== 结果类 ====================

    public static class CulturalNote {
        public final String title, content, zh;
        public CulturalNote(String title, String content, String zh) {
            this.title = title; this.content = content; this.zh = zh;
        }
    }

    public static class CulturalNotesResult extends AIClient.AIResultBase {
        public CulturalNote[] notes;

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", error == null);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            map.put("notes", notes);
            if (rawResponse != null && !rawResponse.isEmpty()) {
                map.put("rawResponse", rawResponse.length() > 3000
                        ? rawResponse.substring(0, 3000) + "..." : rawResponse);
            }
            return GsonUtil.toJson(map);
        }
    }

    public static class WordChainResult extends AIClient.AIResultBase {
        public boolean valid;
        public String word;
        public String reason;

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", error == null);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            map.put("valid", valid);
            map.put("word", word);
            map.put("reason", reason);
            return GsonUtil.toJson(map);
        }
    }

    public static class RolePlayResult extends AIClient.AIResultBase {
        public String narratorEn;
        public String narratorZh;
        public String characterName;
        public String characterDialogue;
        public String suggestion;

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", error == null);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            map.put("narratorEn", narratorEn);
            map.put("narratorZh", narratorZh);
            map.put("characterName", characterName);
            map.put("characterDialogue", characterDialogue);
            map.put("suggestion", suggestion);
            return GsonUtil.toJson(map);
        }
    }

    public static class TranslationResult extends AIClient.AIResultBase {
        public String originalText;
        public String translation;

        public boolean isSuccess() {
            return error == null && translation != null && !translation.isEmpty();
        }

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", isSuccess());
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            if (translation != null) map.put("translation", translation);
            return GsonUtil.toJson(map);
        }
    }
}
