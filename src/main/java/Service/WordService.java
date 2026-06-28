package Service;

import DAO.WordDAO;
import DAO.WordDAOImpl;
import Entities.AiWordDic;
import Entities.WordBase;

import java.util.List;
import java.util.Map;

public class WordService {
    private final WordDAO wordDAO = new WordDAOImpl();

    /** 学习阶段 → 对应查词结果的可读标签 */
    private static final Map<String, Integer> STAGE_EXPECTED_VOCAB = Map.of(
            "初中", 2500,
            "高中", 4500,
            "四级", 5500,
            "六级", 6500,
            "考研", 7500,
            "托福", 10000
    );

    /**
     * 根据用户学习阶段查词。
     * 返回结果结构：
     * - stageResults: 阶段词库中的匹配列表（可能多条）
     * - aiResult: AI 详解结果（仅当阶段词库无结果时尝试）
     * - isBeyondStage: 是否超出当前阶段
     */
    public WordSearchResult searchWord(String word, String studyPurpose) {
        WordSearchResult result = new WordSearchResult();
        result.word = word;
        result.studyPurpose = studyPurpose;

        if (isBlank(word) || isBlank(studyPurpose)) {
            result.error = "参数无效";
            return result;
        }

        // 1. 查询阶段词库
        List<WordBase> stageResults = wordDAO.findByWordAndStage(word, studyPurpose);
        result.stageResults = stageResults;

        if (!stageResults.isEmpty()) {
            result.foundInStage = true;
            result.multiMeaning = stageResults.size() > 1;
            return result;
        }

        // 2. 阶段词库未命中 → 查询 AI 动态词典
        result.foundInStage = false;
        result.isBeyondStage = true;

        AiWordDic aiResult = wordDAO.findLatestAiDic(word);
        if (aiResult != null) {
            result.aiResult = aiResult;
            result.foundInAiDic = true;
        }

        return result;
    }

    /**
     * 获取阶段期望词汇量
     */
    public static int getExpectedVocab(String stage) {
        return STAGE_EXPECTED_VOCAB.getOrDefault(stage, 0);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** 查词结果封装类 */
    public static class WordSearchResult {
        public String word;
        public String studyPurpose;
        public boolean foundInStage;
        public boolean multiMeaning;
        public List<WordBase> stageResults;
        public boolean foundInAiDic;
        public boolean isBeyondStage;
        public AiWordDic aiResult;
        public String error;

        public String toJson() {
            if (error != null) {
                return "{\"success\":false,\"message\":\"" + escape(error) + "\"}";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true");
            sb.append(",\"word\":\"").append(escape(word)).append("\"");
            sb.append(",\"studyPurpose\":\"").append(escape(studyPurpose)).append("\"");
            sb.append(",\"foundInStage\":").append(foundInStage);
            sb.append(",\"isBeyondStage\":").append(isBeyondStage);
            sb.append(",\"multiMeaning\":").append(multiMeaning);

            // 阶段词库结果
            sb.append(",\"stageResults\":[");
            if (stageResults != null && !stageResults.isEmpty()) {
                for (int i = 0; i < stageResults.size(); i++) {
                    if (i > 0) sb.append(",");
                    WordBase w = stageResults.get(i);
                    sb.append("{");
                    sb.append("\"id\":").append(w.getId()).append(",");
                    sb.append("\"word\":\"").append(escape(w.getWord())).append("\",");
                    sb.append("\"phonetic\":\"").append(escape(w.getPhonetic())).append("\",");
                    sb.append("\"translation\":\"").append(escape(w.getTranslation())).append("\"");
                    sb.append("}");
                }
            }
            sb.append("]");

            // AI 词典结果
            sb.append(",\"foundInAiDic\":").append(foundInAiDic);
            if (aiResult != null) {
                sb.append(",\"aiResult\":{");
                sb.append("\"aidicId\":").append(aiResult.getAidicId()).append(",");
                sb.append("\"word\":\"").append(escape(aiResult.getWord())).append("\",");
                sb.append("\"phonetic\":\"").append(escape(aiResult.getPhonetic())).append("\",");
                sb.append("\"translation\":\"").append(escape(aiResult.getTranslation())).append("\",");
                sb.append("\"explanation\":\"").append(escape(aiResult.getExplanation())).append("\",");
                sb.append("\"likeCount\":").append(aiResult.getLikeCount()).append(",");
                sb.append("\"dislikeCount\":").append(aiResult.getDislikeCount());
                sb.append("}");
            }

            sb.append("}");
            return sb.toString();
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}
