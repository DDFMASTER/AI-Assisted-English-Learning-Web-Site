package Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import Utils.ConfigUtil;

/**
 * AI 服务 — 调用 DeepSeek API 为英文单词生成例句。
 * 使用 Java 内置 HttpClient，无需额外依赖。
 */
public class AIService {

    private static final String API_URL = ConfigUtil.get("ai.api.url");
    private static final String API_KEY = ConfigUtil.get("ai.api.key");
    private static final String MODEL = ConfigUtil.get("ai.api.model", "deepseek-v4-flash");
    private static final int TIMEOUT_SECONDS = ConfigUtil.getInt("ai.api.timeout_seconds", 15);

    private static final String SYSTEM_PROMPT =
            ConfigUtil.readResourceText("prompts/word-examples.txt");

    private final HttpClient httpClient;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    // ========== AI 单词释义查词 ==========

    private static final String WORD_LOOKUP_PROMPT =
            ConfigUtil.readResourceText("prompts/word-lookup.txt");

    // ========== 段落翻译 ==========

    private static final String TRANSLATION_SYSTEM_PROMPT =
            ConfigUtil.readResourceText("prompts/translation.txt");

    // ========== 文化背景分析 ==========

    private static final String CULTURE_SYSTEM_PROMPT =
            ConfigUtil.readResourceText("prompts/culture-analysis.txt");

    /**
     * 分析文章中的文化背景知识。
     * @param articleContent 文章全文
     * @return CulturalNotesResult
     */
    public CulturalNotesResult analyzeCulture(String articleContent) {
        CulturalNotesResult result = new CulturalNotesResult();
        long startTime = System.currentTimeMillis();

        // 发送完整文章（最多 8000 字符）
        String content = articleContent.length() > 8000
                ? articleContent.substring(0, 8000)
                : articleContent;

        String responseContent = callDeepSeek(CULTURE_SYSTEM_PROMPT, content, 25, result);

        if (responseContent != null) {
            List<CulturalNote> notes = new ArrayList<>();
            int pos = 0;
            while ((pos = responseContent.indexOf("\"title\":\"", pos)) != -1) {
                pos += 9;
                String title = extractStringValue(responseContent, pos);
                pos = responseContent.indexOf("\"content\":\"", pos);
                if (pos == -1) break;
                pos += 11;
                String noteContent = extractStringValue(responseContent, pos);
                // 尝试提取 zh 字段
                String zh = "";
                int zhPos = responseContent.indexOf("\"zh\":\"", pos);
                if (zhPos != -1) {
                    zhPos += 6;
                    zh = extractStringValue(responseContent, zhPos);
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

    // ========== 阅读理解出题 ==========

    private static final String QUIZ_SYSTEM_PROMPT =
            ConfigUtil.readResourceText("prompts/quiz-generation.txt");

    // ========== 测评出题（单题快速版本） ==========

    private static final String ASSESSMENT_SINGLE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-single.txt");

    // ========== 测评出题 ==========

    private static final String ASSESSMENT_GENERATE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-generate.txt");

    // ========== 测评评估 ==========

    private static final String ASSESSMENT_EVALUATE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-evaluate.txt");

    /**
     * 基于文章生成阅读理解选择题。
     * @param articleContent 文章全文
     * @return QuizResult
     */
    public QuizResult generateQuiz(String articleContent) {
        QuizResult result = new QuizResult();
        long startTime = System.currentTimeMillis();

        // 发送完整文章（最多 8000 字符）
        String content = articleContent.length() > 8000
                ? articleContent.substring(0, 8000)
                : articleContent;

        String responseContent = callDeepSeek(QUIZ_SYSTEM_PROMPT, content, 25, result);

        if (responseContent != null) {
            // 提取 mainIdea
            int miPos = responseContent.indexOf("\"mainIdea\":\"");
            if (miPos != -1) {
                miPos += 12;
                result.mainIdea = extractStringValue(responseContent, miPos);
            }

            List<QuizQuestion> questions = new ArrayList<>();
            int pos = 0;
            while ((pos = responseContent.indexOf("\"question\":\"", pos)) != -1) {
                pos += 12;
                String question = extractStringValue(responseContent, pos);

                // 提取 optionA/B/C/D（独立字段，稳定可靠）
                String[] opts = new String[4];
                String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
                for (int i = 0; i < 4; i++) {
                    int optPos = responseContent.indexOf(optKeys[i], pos);
                    if (optPos == -1) break;
                    optPos += optKeys[i].length();
                    opts[i] = extractStringValue(responseContent, optPos);
                }

                // 提取 answer
                pos = responseContent.indexOf("\"answer\":", pos);
                if (pos == -1) break;
                pos += 9;
                int answer = 0;
                while (pos < responseContent.length() && responseContent.charAt(pos) >= '0'
                        && responseContent.charAt(pos) <= '9') {
                    answer = answer * 10 + (responseContent.charAt(pos) - '0');
                    pos++;
                }

                // 提取 explanation
                pos = responseContent.indexOf("\"explanation\":\"", pos);
                if (pos == -1) break;
                pos += 15;
                String explanation = extractStringValue(responseContent, pos);

                if (question != null && opts[0] != null && opts[1] != null
                        && opts[2] != null && opts[3] != null) {
                    questions.add(new QuizQuestion(
                            question,
                            opts,
                            answer,
                            explanation != null ? explanation : ""
                    ));
                }
            }
            result.questions = questions.toArray(new QuizQuestion[0]);
            System.out.println("[AIService] 出题结果: 提取到 " + result.questions.length
                    + " 道题, mainIdea=" + (result.mainIdea != null ? "有" : "无"));

            if (result.questions.length == 0) {
                result.error = "AI 返回内容解析失败，content长度=" + responseContent.length()
                        + "，前200字符: "
                        + (responseContent.length() > 200
                            ? responseContent.substring(0, 200) + "..."
                            : responseContent);
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    // ========== 测评出题与评估 ==========

    /**
     * 快速生成单道测评题（用于首题即时返回）。
     */
    public AssessmentQuestion generateSingleQuestion(String studyPurpose) {
        return generateSingleQuestion(studyPurpose, null);
    }

    /** 生成单道题，可指定必须避开的答案索引（连续 3 题同答案时使用） */
    public AssessmentQuestion generateSingleQuestion(String studyPurpose, Integer avoidAnswer) {
        String prompt = ASSESSMENT_SINGLE_PROMPT.replace("{studyPurpose}", studyPurpose);
        if (avoidAnswer != null && avoidAnswer >= 0 && avoidAnswer <= 3) {
            char avoid = (char) ('A' + avoidAnswer);
            prompt += "\nIMPORTANT: Do NOT set answer to " + avoid
                    + ". Previous 3 questions all had answer " + avoid + ".";
        }
        String userContent = "Generate 1 reading passage for " + studyPurpose + " level.";

        String responseContent = callDeepSeek(prompt, userContent, 20, new AIResultBase());

        if (responseContent != null) {
            return parseOneQuestion(responseContent);
        }
        return null;
    }

    private AssessmentQuestion parseOneQuestion(String json) {
        int pos = 0;

        pos = json.indexOf("\"passage\":\"", pos);
        if (pos == -1) return null;
        pos += 11;
        String passage = extractStringValue(json, pos);

        pos = json.indexOf("\"question\":\"", pos);
        if (pos == -1) return null;
        pos += 12;
        String question = extractStringValue(json, pos);

        String[] opts = new String[4];
        String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
        for (int i = 0; i < 4; i++) {
            int optPos = json.indexOf(optKeys[i], pos);
            if (optPos == -1) return null;
            optPos += optKeys[i].length();
            opts[i] = extractStringValue(json, optPos);
        }

        pos = json.indexOf("\"answer\":", pos);
        if (pos == -1) return null;
        pos += 9;
        int answer = 0;
        while (pos < json.length() && json.charAt(pos) >= '0' && json.charAt(pos) <= '9') {
            answer = answer * 10 + (json.charAt(pos) - '0');
            pos++;
        }

        pos = json.indexOf("\"explanation\":\"", pos);
        if (pos == -1) return null;
        pos += 15;
        String explanation = extractStringValue(json, pos);

        if (passage != null && question != null && opts[0] != null) {
            return new AssessmentQuestion(passage, question, opts, answer,
                    explanation != null ? explanation : "");
        }
        return null;
    }

    /**
     * 根据学习阶段生成 10 段短文章及对应的阅读理解选择题。
     * @param studyPurpose 用户学习阶段（初中/高中/四级/六级/考研/托福）
     * @return AssessmentGenerateResult
     */
    public AssessmentGenerateResult generateAssessment(String studyPurpose) {
        AssessmentGenerateResult result = new AssessmentGenerateResult();
        long startTime = System.currentTimeMillis();

        String prompt = ASSESSMENT_GENERATE_PROMPT.replace("{studyPurpose}", studyPurpose);
        String userContent = "Generate 10 short reading passages for " + studyPurpose + " level students.";

        String responseContent = callDeepSeek(prompt, userContent, 60, result);

        if (responseContent != null) {
            List<AssessmentQuestion> questions = new ArrayList<>();
            int pos = 0;
            int qCount = 0;
            while ((pos = responseContent.indexOf("\"passage\":\"", pos)) != -1 && qCount < 10) {
                pos += 11;
                String passage = extractStringValue(responseContent, pos);

                pos = responseContent.indexOf("\"question\":\"", pos);
                if (pos == -1) break;
                pos += 12;
                String question = extractStringValue(responseContent, pos);

                String[] opts = new String[4];
                String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
                for (int i = 0; i < 4; i++) {
                    int optPos = responseContent.indexOf(optKeys[i], pos);
                    if (optPos == -1) break;
                    optPos += optKeys[i].length();
                    opts[i] = extractStringValue(responseContent, optPos);
                }

                pos = responseContent.indexOf("\"answer\":", pos);
                if (pos == -1) break;
                pos += 9;
                int answer = 0;
                while (pos < responseContent.length() && responseContent.charAt(pos) >= '0'
                        && responseContent.charAt(pos) <= '9') {
                    answer = answer * 10 + (responseContent.charAt(pos) - '0');
                    pos++;
                }

                pos = responseContent.indexOf("\"explanation\":\"", pos);
                if (pos == -1) break;
                pos += 15;
                String explanation = extractStringValue(responseContent, pos);

                if (passage != null && question != null && opts[0] != null) {
                    questions.add(new AssessmentQuestion(
                            passage, question, opts, answer,
                            explanation != null ? explanation : ""));
                    qCount++;
                }
            }
            result.questions = questions.toArray(new AssessmentQuestion[0]);

            if (result.questions.length == 0) {
                result.error = "AI 返回内容解析失败，未提取到题目";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * 根据用户的答题情况评估英语能力，返回分项得分和 CEFR 等级。
     * @param questionsJson 题目及正确答案的 JSON 字符串
     * @param answersJson   用户答案的 JSON 字符串
     * @return AssessmentEvaluateResult
     */
    public AssessmentEvaluateResult evaluateAssessment(String questionsJson, String answersJson) {
        AssessmentEvaluateResult result = new AssessmentEvaluateResult();
        long startTime = System.currentTimeMillis();

        String userContent = "Questions and correct answers:\n" + questionsJson
                + "\n\nUser's answers:\n" + answersJson;

        String responseContent = callDeepSeek(ASSESSMENT_EVALUATE_PROMPT, userContent, 30, result);

        if (responseContent != null) {
            // 提取 overallScore
            int scorePos = responseContent.indexOf("\"overallScore\":");
            if (scorePos != -1) {
                scorePos += 15;
                result.overallScore = parseIntFromContent(responseContent, scorePos);
            }

            // 提取 cefrLevel
            int cefrPos = responseContent.indexOf("\"cefrLevel\":\"");
            if (cefrPos != -1) {
                cefrPos += 13;
                result.cefrLevel = extractStringValue(responseContent, cefrPos);
            }

            // 提取各维度分数
            if (responseContent.contains("\"dimensions\":")) {
                result.vocabulary = extractDimension(responseContent, "vocabulary");
                result.grammar = extractDimension(responseContent, "grammar");
                result.reading = extractDimension(responseContent, "reading");
                result.culture = extractDimension(responseContent, "culture");
                result.logic = extractDimension(responseContent, "logic");
            }

            if (result.cefrLevel == null) {
                result.error = "AI 评估结果解析失败";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * 从 JSON 字符串中提取指定键的整数值
     */
    private int extractDimension(String content, String key) {
        String searchKey = "\"" + key + "\":";
        int pos = content.indexOf(searchKey);
        if (pos == -1) return 0;
        pos += searchKey.length();
        return parseIntFromContent(content, pos);
    }

    /**
     * 从指定位置解析整数
     */
    private int parseIntFromContent(String s, int pos) {
        int val = 0;
        while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
            val = val * 10 + (s.charAt(pos) - '0');
            pos++;
        }
        return val;
    }

    // ========== 通用 API 调用 ==========

    /**
     * 调用 DeepSeek API，返回提取的 content 字符串。
     * 出错时设置 result.error 并返回 null。
     */
    private String callDeepSeek(String systemPrompt, String userContent,
                                int timeoutSeconds, AIResultBase result) {
        try {
            String requestBody = buildRequestBody(systemPrompt, userContent);

            System.out.println("[AIService] 请求 URL: " + API_URL);
            System.out.println("[AIService] 请求体(前300字符): "
                    + (requestBody.length() > 300
                        ? requestBody.substring(0, 300) + "..."
                        : requestBody));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            result.httpStatus = response.statusCode();
            System.out.println("[AIService] 响应状态: " + response.statusCode());

            if (response.statusCode() != 200) {
                result.rawResponse = response.body();
                result.error = "AI 服务返回错误状态码: " + response.statusCode();
                return null;
            }

            String body = response.body();
            result.rawResponse = body;
            System.out.println("[AIService] 响应体(前500字符): "
                    + (body.length() > 500 ? body.substring(0, 500) + "..." : body));

            String content = extractContent(body);
            if (content == null || content.isEmpty()) {
                result.error = "AI 服务返回内容为空";
                return null;
            }

            // 去除可能的 markdown 代码块包裹（```json ... ```）
            content = stripMarkdownFence(content);

            // 规范化 JSON：去除字符串外的空格（AI 可能输出 "key": "val" 而非 "key":"val"）
            content = normalizeJson(content);

            System.out.println("[AIService] 提取的 content(前300字符): "
                    + (content.length() > 300 ? content.substring(0, 300) + "..." : content));

            return content;

        } catch (Exception e) {
            result.error = "AI 调用失败: " + e.getMessage();
            System.out.println("[AIService] 异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ========== 单词例句生成 ==========

    /**
     * 为指定单词生成 3 个例句。
     * @param word 英文单词
     * @return AIExampleResult，包含例句列表或错误信息
     */
    public AIExampleResult generateExamples(String word) {
        AIExampleResult result = new AIExampleResult();
        result.word = word;
        long startTime = System.currentTimeMillis();

        String content = callDeepSeek(SYSTEM_PROMPT, word, TIMEOUT_SECONDS, result);

        if (content != null) {
            parseExamples(content, result);

            if (result.examples == null || result.examples.length == 0) {
                result.error = "AI 例句解析失败：未能从响应中提取例句，耗时: "
                        + (System.currentTimeMillis() - startTime)
                        + "ms，AI 返回内容: "
                        + (content.length() > 300 ? content.substring(0, 300) + "..." : content);
                result.rawResponse = "=== AI 返回的 content ===\n" + content
                        + "\n\n=== API 原始响应 ===\n" + result.rawResponse;
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * 翻译英文段落为中文。
     * @param paragraph 英文段落文本
     * @return TranslationResult，包含中文译文或错误信息
     */
    public TranslationResult translateParagraph(String paragraph) {
        TranslationResult result = new TranslationResult();
        result.originalText = paragraph;
        long startTime = System.currentTimeMillis();

        String content = callDeepSeek(TRANSLATION_SYSTEM_PROMPT, paragraph, 20, result);

        if (content != null) {
            // 提取 translation 字段
            for (String key : new String[]{"\"translation\":\"", "\"translation\": \"", "\"translation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    String translation = extractStringValue(content, start);
                    if (translation != null && !translation.isEmpty()) {
                        result.translation = translation;
                        break;
                    }
                }
            }
            if (result.translation == null) {
                result.error = "翻译结果解析失败";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * AI 查词：当所有词书和 ai_word_dic 都未命中时，由 AI 搜索单词释义。
     * @param word 英文单词
     * @return AiWordLookupResult，成功时包含音标/释义/解释
     */
    public AiWordLookupResult lookupWordByAI(String word) {
        AiWordLookupResult result = new AiWordLookupResult();
        result.word = word;
        long startTime = System.currentTimeMillis();

        String content = callDeepSeek(WORD_LOOKUP_PROMPT, word, 20, new AIResultBase());

        if (content != null) {
            // 解析 phonetic
            for (String key : new String[]{"\"phonetic\":\"", "\"phonetic\": \"", "\"phonetic\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.phonetic = extractStringValue(content, start);
                    break;
                }
            }
            // 解析 translation
            for (String key : new String[]{"\"translation\":\"", "\"translation\": \"", "\"translation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.translation = extractStringValue(content, start);
                    break;
                }
            }
            // 解析 explanation
            for (String key : new String[]{"\"explanation\":\"", "\"explanation\": \"", "\"explanation\" :\""}) {
                int start = content.indexOf(key);
                if (start != -1) {
                    start += key.length();
                    result.explanation = extractStringValue(content, start);
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

    /** AI 查词结果 */
    public static class AiWordLookupResult extends AIResultBase {
        public String word;
        public String phonetic;
        public String translation;
        public String explanation;
    }

    /**
     * 构建发给 DeepSeek 的请求体 JSON（通用版本）
     */
    private String buildRequestBody(String systemPrompt, String userContent) {
        String safeSystem = escapeJson(systemPrompt);
        String safeUser = escapeJson(userContent);

        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + safeSystem + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + safeUser + "\"}"
                + "],"
                + "\"response_format\":{\"type\":\"json_object\"}"
                + "}";
    }

    /**
     * 规范化 JSON：去除字符串外的空格/换行/制表符，
     * 使 "key": "val" 变为 "key":"val"，确保统一的紧凑格式便于解析。
     */
    private String normalizeJson(String json) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
                sb.append(c);
            } else if (!inString && (c == ' ' || c == '\n' || c == '\r' || c == '\t')) {
                // 跳过字符串外的空白
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 去除 AI 输出中可能包裹的 markdown 代码块标记
     */
    private String stripMarkdownFence(String content) {
        String trimmed = content.trim();
        // 去掉开头的 ```json 或 ```
        if (trimmed.startsWith("```")) {
            int endOfFirstLine = trimmed.indexOf('\n');
            if (endOfFirstLine != -1) {
                trimmed = trimmed.substring(endOfFirstLine + 1);
            }
        }
        // 去掉结尾的 ```
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    /**
     * 从 DeepSeek 响应中提取 choices[0].message.content
     */
    private String extractContent(String responseBody) {
        // 尝试多种可能的 key 格式（有无空格）
        for (String key : new String[]{"\"content\":\"", "\"content\": \"", "\"content\" :\""}) {
            int start = responseBody.indexOf(key);
            if (start != -1) {
                start += key.length();

                // 从 content 开始位置找到未转义的闭合引号
                StringBuilder content = new StringBuilder();
                for (int i = start; i < responseBody.length(); i++) {
                    char c = responseBody.charAt(i);
                    if (c == '\\' && i + 1 < responseBody.length()) {
                        char next = responseBody.charAt(i + 1);
                        content.append(c);
                        content.append(next);
                        i++;
                    } else if (c == '"') {
                        break;
                    } else {
                        content.append(c);
                    }
                }

                // 将 JSON 字符串中的转义序列还原
                return unescapeJson(content.toString());
            }
        }
        return null;
    }

    /**
     * 解析 AI 返回的 content JSON，提取 examples
     */
    private void parseExamples(String contentJson, AIExampleResult result) {
        List<String[]> examples = new ArrayList<>();

        // 尝试多种 key 格式（有无空格）
        String[] enKeys = {"\"en\":\"", "\"en\": \"", "\"en\" :\""};
        String[] zhKeys = {"\"zh\":\"", "\"zh\": \"", "\"zh\" :\""};

        int pos = 0;
        while (pos < contentJson.length() && examples.size() < 3) {
            // 找 "en" 键
            int enStart = -1;
            int enKeyLen = 0;
            for (String k : enKeys) {
                int idx = contentJson.indexOf(k, pos);
                if (idx != -1 && (enStart == -1 || idx < enStart)) {
                    enStart = idx;
                    enKeyLen = k.length();
                }
            }
            if (enStart == -1) break;

            pos = enStart + enKeyLen;
            String en = extractStringValue(contentJson, pos);

            // 找紧随的 "zh" 键
            int zhStart = -1;
            int zhKeyLen = 0;
            for (String k : zhKeys) {
                int idx = contentJson.indexOf(k, pos);
                if (idx != -1 && (zhStart == -1 || idx < zhStart)) {
                    zhStart = idx;
                    zhKeyLen = k.length();
                }
            }
            if (zhStart == -1) break;

            pos = zhStart + zhKeyLen;
            String zh = extractStringValue(contentJson, pos);

            if (en != null && zh != null) {
                examples.add(new String[]{en, zh});
            }
        }

        result.examples = examples.toArray(new String[0][]);
    }

    /**
     * 从指定位置提取 "..." 中的字符串值，返回位置更新到闭合引号之后
     */
    private String extractStringValue(String json, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> { sb.append('\\'); sb.append(next); }
                }
                i++;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将 JSON 转义字符串还原（处理 \\\", \\n, \\\\ 等）
     */
    private String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * JSON 字符串转义
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * AI 结果基类，包含所有结果共有的字段
     */
    public static class AIResultBase {
        public String error;
        public int httpStatus;
        public long duration;
        public String rawResponse;
    }

    /**
     * AI 例句生成结果
     */
    public static class AIExampleResult extends AIResultBase {
        public String word;
        public String[][] examples; // 每个元素 [en, zh]

        public boolean isSuccess() {
            return error == null && examples != null && examples.length > 0;
        }

        /**
         * 序列化为前端可用的 JSON
         */
        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(isSuccess());
            sb.append(",\"word\":\"").append(escapeJsonStatic(word)).append("\"");
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }

            sb.append(",\"examples\":[");
            if (examples != null) {
                for (int i = 0; i < examples.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"en\":\"").append(escapeJsonStatic(examples[i][0])).append("\",");
                    sb.append("\"zh\":\"").append(escapeJsonStatic(examples[i][1])).append("\"");
                    sb.append("}");
                }
            }
            sb.append("]");

            // 返回原始/调试信息（前端诊断用，截断防过大）
            if (rawResponse != null && !rawResponse.isEmpty()) {
                String truncated = rawResponse.length() > 3000
                        ? rawResponse.substring(0, 3000) + "...[截断]"
                        : rawResponse;
                sb.append(",\"rawResponse\":\"")
                        .append(escapeJsonStatic(truncated))
                        .append("\"");
            }

            sb.append("}");
            return sb.toString();
        }

        private static String escapeJsonStatic(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder(s.length());
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    // ========== 文化背景分析结果 ==========

    public static class CulturalNote {
        public final String title;
        public final String content;
        public final String zh;

        public CulturalNote(String title, String content, String zh) {
            this.title = title;
            this.content = content;
            this.zh = zh;
        }
    }

    public static class CulturalNotesResult extends AIResultBase {
        public CulturalNote[] notes;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(error == null);
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }

            sb.append(",\"notes\":[");
            if (notes != null) {
                for (int i = 0; i < notes.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"title\":\"").append(escapeJsonStatic(notes[i].title)).append("\",");
                    sb.append("\"content\":\"").append(escapeJsonStatic(notes[i].content)).append("\",");
                    sb.append("\"zh\":\"").append(escapeJsonStatic(notes[i].zh)).append("\"");
                    sb.append("}");
                }
            }
            sb.append("]");

            if (rawResponse != null && !rawResponse.isEmpty()) {
                String truncated = rawResponse.length() > 3000
                        ? rawResponse.substring(0, 3000) + "..."
                        : rawResponse;
                sb.append(",\"rawResponse\":\"")
                        .append(escapeJsonStatic(truncated))
                        .append("\"");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    // ========== 阅读理解出题结果 ==========

    public static class QuizQuestion {
        public final String question;
        public final String[] options;
        public final int answer;
        public final String explanation;

        public QuizQuestion(String question, String[] options, int answer, String explanation) {
            this.question = question;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
        }
    }

    public static class QuizResult extends AIResultBase {
        public String mainIdea;
        public QuizQuestion[] questions;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(error == null);
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }

            sb.append(",\"mainIdea\":\"")
                    .append(escapeJsonStatic(mainIdea != null ? mainIdea : ""))
                    .append("\"");
            sb.append(",\"questions\":[");
            if (questions != null) {
                for (int i = 0; i < questions.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"question\":\"")
                            .append(escapeJsonStatic(questions[i].question)).append("\",");
                    sb.append("\"options\":[");
                    for (int j = 0; j < questions[i].options.length; j++) {
                        if (j > 0) sb.append(",");
                        sb.append("\"")
                                .append(escapeJsonStatic(questions[i].options[j]))
                                .append("\"");
                    }
                    sb.append("],");
                    sb.append("\"answer\":").append(questions[i].answer).append(",");
                    sb.append("\"explanation\":\"")
                            .append(escapeJsonStatic(questions[i].explanation)).append("\"");
                    sb.append("}");
                }
            }
            sb.append("]");

            if (rawResponse != null && !rawResponse.isEmpty()) {
                String truncated = rawResponse.length() > 3000
                        ? rawResponse.substring(0, 3000) + "..."
                        : rawResponse;
                sb.append(",\"rawResponse\":\"")
                        .append(escapeJsonStatic(truncated))
                        .append("\"");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    // ========== 测评出题结果 ==========

    public static class AssessmentQuestion {
        public final String passage;
        public final String question;
        public final String[] options;
        public final int answer;
        public final String explanation;

        public AssessmentQuestion(String passage, String question, String[] options,
                                   int answer, String explanation) {
            this.passage = passage;
            this.question = question;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
        }
    }

    public static class AssessmentGenerateResult extends AIResultBase {
        public AssessmentQuestion[] questions;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(error == null);
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }

            sb.append(",\"questions\":[");
            if (questions != null) {
                for (int i = 0; i < questions.length; i++) {
                    if (i > 0) sb.append(",");
                    AssessmentQuestion q = questions[i];
                    sb.append("{");
                    sb.append("\"passage\":\"").append(escapeJsonStatic(q.passage)).append("\",");
                    sb.append("\"question\":\"").append(escapeJsonStatic(q.question)).append("\",");
                    sb.append("\"optionA\":\"").append(escapeJsonStatic(q.options[0])).append("\",");
                    sb.append("\"optionB\":\"").append(escapeJsonStatic(q.options[1])).append("\",");
                    sb.append("\"optionC\":\"").append(escapeJsonStatic(q.options[2])).append("\",");
                    sb.append("\"optionD\":\"").append(escapeJsonStatic(q.options[3])).append("\",");
                    sb.append("\"answer\":").append(q.answer).append(",");
                    sb.append("\"explanation\":\"").append(escapeJsonStatic(q.explanation)).append("\"");
                    sb.append("}");
                }
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    public static class AssessmentEvaluateResult extends AIResultBase {
        public int overallScore;
        public String cefrLevel;
        public int vocabulary;
        public int grammar;
        public int reading;
        public int culture;
        public int logic;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(error == null);
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }

            sb.append(",\"overallScore\":").append(overallScore);
            sb.append(",\"cefrLevel\":\"").append(escapeJsonStatic(cefrLevel != null ? cefrLevel : "")).append("\"");
            sb.append(",\"vocabulary\":").append(vocabulary);
            sb.append(",\"grammar\":").append(grammar);
            sb.append(",\"reading\":").append(reading);
            sb.append(",\"culture\":").append(culture);
            sb.append(",\"logic\":").append(logic);
            sb.append("}");
            return sb.toString();
        }
    }

    // ========== 段落翻译结果 ==========

    public static class TranslationResult extends AIResultBase {
        public String originalText;
        public String translation;

        public boolean isSuccess() {
            return error == null && translation != null && !translation.isEmpty();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":").append(isSuccess());
            sb.append(",\"httpStatus\":").append(httpStatus);
            sb.append(",\"duration\":").append(duration);

            if (error != null) {
                sb.append(",\"message\":\"").append(escapeJsonStatic(error)).append("\"");
            }
            if (translation != null) {
                sb.append(",\"translation\":\"").append(escapeJsonStatic(translation)).append("\"");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    private static String escapeJsonStatic(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
