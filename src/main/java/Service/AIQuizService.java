package Service;

import Utils.ConfigUtil;
import Utils.GsonUtil;
import Utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 测验服务 — 阅读理解出题、测评出题与评估。
 */
public class AIQuizService {

    private static final String QUIZ_PROMPT =
            ConfigUtil.readResourceText("prompts/quiz-generation.txt");

    private static final String ASSESSMENT_SINGLE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-single.txt");

    private static final String ASSESSMENT_GENERATE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-generate.txt");

    private static final String ASSESSMENT_EVALUATE_PROMPT =
            ConfigUtil.readResourceText("prompts/assessment-evaluate.txt");

    private final AIClient client;

    public AIQuizService(AIClient client) {
        this.client = client;
    }

    // ==================== 阅读理解出题 ====================

    /** 基于文章生成阅读理解选择题 */
    public QuizResult generateQuiz(String articleContent) {
        QuizResult result = new QuizResult();
        long startTime = System.currentTimeMillis();

        String content = articleContent.length() > 8000
                ? articleContent.substring(0, 8000) : articleContent;

        String response = client.call(QUIZ_PROMPT, content, 25, result);

        if (response != null) {
            int miPos = response.indexOf("\"mainIdea\":\"");
            if (miPos != -1) {
                miPos += 12;
                result.mainIdea = client.extractStringValue(response, miPos);
            }

            List<QuizQuestion> questions = new ArrayList<>();
            int pos = 0;
            while ((pos = response.indexOf("\"question\":\"", pos)) != -1) {
                pos += 12;
                String question = client.extractStringValue(response, pos);
                String[] opts = new String[4];
                String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
                for (int i = 0; i < 4; i++) {
                    int optPos = response.indexOf(optKeys[i], pos);
                    if (optPos == -1) break;
                    optPos += optKeys[i].length();
                    opts[i] = client.extractStringValue(response, optPos);
                }
                pos = response.indexOf("\"answer\":", pos);
                if (pos == -1) break;
                pos += 9;
                int answer = parseIntFromContent(response, pos);

                pos = response.indexOf("\"explanation\":\"", pos);
                if (pos == -1) break;
                pos += 15;
                String explanation = client.extractStringValue(response, pos);

                if (question != null && opts[0] != null && opts[1] != null
                        && opts[2] != null && opts[3] != null) {
                    questions.add(new QuizQuestion(question, opts, answer,
                            explanation != null ? explanation : ""));
                }
            }
            result.questions = questions.toArray(new QuizQuestion[0]);

            if (result.questions.length == 0) {
                result.error = "AI 返回内容解析失败";
            }
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    // ==================== 测评出题 ====================

    public AssessmentQuestion generateSingleQuestion(String studyPurpose) {
        return generateSingleQuestion(studyPurpose, null);
    }

    public AssessmentQuestion generateSingleQuestion(String studyPurpose, Integer avoidAnswer) {
        String prompt = ASSESSMENT_SINGLE_PROMPT.replace("{studyPurpose}", studyPurpose);
        if (avoidAnswer != null && avoidAnswer >= 0 && avoidAnswer <= 3) {
            char avoid = (char) ('A' + avoidAnswer);
            prompt += "\nIMPORTANT: Do NOT set answer to " + avoid + ". Previous 3 questions all had answer " + avoid + ".";
        }
        String userContent = "Generate 1 reading passage for " + studyPurpose + " level.";
        String response = client.call(prompt, userContent, 20, new AIClient.AIResultBase());
        if (response != null) return parseOneQuestion(response);
        return null;
    }

    private AssessmentQuestion parseOneQuestion(String json) {
        int pos = 0;
        pos = json.indexOf("\"passage\":\"", pos);
        if (pos == -1) return null;
        pos += 11;
        String passage = client.extractStringValue(json, pos);

        pos = json.indexOf("\"question\":\"", pos);
        if (pos == -1) return null;
        pos += 12;
        String question = client.extractStringValue(json, pos);

        String[] opts = new String[4];
        String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
        for (int i = 0; i < 4; i++) {
            int optPos = json.indexOf(optKeys[i], pos);
            if (optPos == -1) return null;
            optPos += optKeys[i].length();
            opts[i] = client.extractStringValue(json, optPos);
        }

        pos = json.indexOf("\"answer\":", pos);
        if (pos == -1) return null;
        pos += 9;
        int answer = parseIntFromContent(json, pos);

        pos = json.indexOf("\"explanation\":\"", pos);
        if (pos == -1) return null;
        pos += 15;
        String explanation = client.extractStringValue(json, pos);

        if (passage != null && question != null && opts[0] != null) {
            return new AssessmentQuestion(passage, question, opts, answer,
                    explanation != null ? explanation : "");
        }
        return null;
    }

    /** 根据学习阶段生成 10 段短文章及对应的阅读理解题 */
    public AssessmentGenerateResult generateAssessment(String studyPurpose) {
        AssessmentGenerateResult result = new AssessmentGenerateResult();
        long startTime = System.currentTimeMillis();

        String prompt = ASSESSMENT_GENERATE_PROMPT.replace("{studyPurpose}", studyPurpose);
        String userContent = "Generate 10 short reading passages for " + studyPurpose + " level students.";
        String response = client.call(prompt, userContent, 60, result);

        if (response != null) {
            List<AssessmentQuestion> questions = new ArrayList<>();
            int pos = 0, qCount = 0;
            while ((pos = response.indexOf("\"passage\":\"", pos)) != -1 && qCount < 10) {
                pos += 11;
                String passage = client.extractStringValue(response, pos);
                pos = response.indexOf("\"question\":\"", pos);
                if (pos == -1) break;
                pos += 12;
                String question = client.extractStringValue(response, pos);
                String[] opts = new String[4];
                String[] optKeys = {"\"optionA\":\"", "\"optionB\":\"", "\"optionC\":\"", "\"optionD\":\""};
                for (int i = 0; i < 4; i++) {
                    int optPos = response.indexOf(optKeys[i], pos);
                    if (optPos == -1) break;
                    optPos += optKeys[i].length();
                    opts[i] = client.extractStringValue(response, optPos);
                }
                pos = response.indexOf("\"answer\":", pos);
                if (pos == -1) break;
                pos += 9;
                int answer = parseIntFromContent(response, pos);
                pos = response.indexOf("\"explanation\":\"", pos);
                if (pos == -1) break;
                pos += 15;
                String explanation = client.extractStringValue(response, pos);
                if (passage != null && question != null && opts[0] != null) {
                    questions.add(new AssessmentQuestion(passage, question, opts, answer,
                            explanation != null ? explanation : ""));
                    qCount++;
                }
            }
            result.questions = questions.toArray(new AssessmentQuestion[0]);
            if (result.questions.length == 0) result.error = "未提取到题目";
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    // ==================== 测评评估 ====================

    public AssessmentEvaluateResult evaluateAssessment(String questionsJson, String answersJson) {
        AssessmentEvaluateResult result = new AssessmentEvaluateResult();
        long startTime = System.currentTimeMillis();

        String userContent = "Questions and correct answers:\n" + questionsJson
                + "\n\nUser's answers:\n" + answersJson;

        String response = client.call(ASSESSMENT_EVALUATE_PROMPT, userContent, 30, result);

        if (response != null) {
            result.overallScore = client.extractInt(response, "overallScore");

            int cefrPos = response.indexOf("\"cefrLevel\":\"");
            if (cefrPos != -1) {
                cefrPos += 13;
                result.cefrLevel = client.extractStringValue(response, cefrPos);
            }

            if (response.contains("\"dimensions\":")) {
                result.vocabulary = client.extractInt(response, "vocabulary");
                result.grammar = client.extractInt(response, "grammar");
                result.reading = client.extractInt(response, "reading");
                result.culture = client.extractInt(response, "culture");
                result.logic = client.extractInt(response, "logic");
            }

            if (result.cefrLevel == null) result.error = "AI 评估结果解析失败";
        }

        result.duration = System.currentTimeMillis() - startTime;
        return result;
    }

    private int parseIntFromContent(String s, int pos) {
        int val = 0;
        while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
            val = val * 10 + (s.charAt(pos) - '0');
            pos++;
        }
        return val;
    }

    // ==================== 结果类 ====================

    public static class QuizQuestion {
        public final String question;
        public final String[] options;
        public final int answer;
        public final String explanation;
        public QuizQuestion(String question, String[] options, int answer, String explanation) {
            this.question = question; this.options = options; this.answer = answer; this.explanation = explanation;
        }
    }

    public static class QuizResult extends AIClient.AIResultBase {
        public String mainIdea;
        public QuizQuestion[] questions;

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", error == null);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            map.put("mainIdea", mainIdea);
            map.put("questions", questions);
            return GsonUtil.toJson(map);
        }
    }

    public static class AssessmentQuestion {
        public final String passage, question, explanation;
        public final String[] options;
        public final int answer;
        public AssessmentQuestion(String passage, String question, String[] options, int answer, String explanation) {
            this.passage = passage; this.question = question; this.options = options;
            this.answer = answer; this.explanation = explanation;
        }
    }

    public static class AssessmentGenerateResult extends AIClient.AIResultBase {
        public AssessmentQuestion[] questions;

        public String toJson() {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", error == null);
            map.put("httpStatus", httpStatus);
            map.put("duration", duration);
            if (error != null) map.put("message", error);
            map.put("questions", questions);
            return GsonUtil.toJson(map);
        }
    }

    public static class AssessmentEvaluateResult extends AIClient.AIResultBase {
        public int overallScore;
        public String cefrLevel;
        public int vocabulary, grammar, reading, culture, logic;

        public String toJson() {
            return GsonUtil.toJson(this);
        }
    }
}
