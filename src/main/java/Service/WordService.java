package Service;

import DAO.WordDAO;
import DAO.WordDAOImpl;
import Entities.AiWordDic;
import Entities.WordBase;

import Utils.GsonUtil;
import Utils.JsonUtil;

import java.util.LinkedHashMap;
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

    /**
     * 跨全部词库查词（用于阅读器中点击单词）。
     * 遍历全部6个词库表，返回所有匹配结果按来源分组。
     */
    public WordLookupResult lookupWordInAllStages(String word) {
        WordLookupResult result = new WordLookupResult();
        result.word = word;

        if (word == null || word.isBlank()) {
            result.error = "参数无效";
            return result;
        }

        // 跨全部词库查询
        Map<String, List<WordBase>> allResults = wordDAO.findByWordInAllTables(word);
        result.stageResults = allResults;

        // 从第一个有结果的词库中提取音标
        for (Map.Entry<String, List<WordBase>> entry : allResults.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String ph = entry.getValue().get(0).getPhonetic();
                if (ph != null && !ph.isBlank()) {
                    result.phonetic = ph;
                    break;
                }
            }
        }

        result.found = !allResults.isEmpty();
        return result;
    }

    /**
     * 按用户学习阶段查词（用于阅读器中点击单词时按 study_purpose 过滤）。
     * 优先查询当前学习阶段词库；若未命中，则按顺序（初中→高中→四级→六级→考研→托福）
     * 检索其他词书，返回第一个匹配结果并标注来源词书。
     *
     * @param word         单词
     * @param studyPurpose 用户学习阶段（初中/高中/四级/六级/考研/托福）
     * @return 查词结果，found=true 时 results 中包含来源词书信息
     */
    public WordLookupResult lookupWordByStage(String word, String studyPurpose) {
        WordLookupResult result = new WordLookupResult();
        result.word = word;

        if (word == null || word.isBlank()) {
            result.error = "参数无效";
            return result;
        }

        if (studyPurpose == null || studyPurpose.isBlank()) {
            // 未指定学习阶段时，回退到全词库查询
            return lookupWordInAllStages(word);
        }

        // 1) 先查当前学习阶段词库
        List<WordBase> stageResults = wordDAO.findByWordAndStage(word, studyPurpose);

        if (!stageResults.isEmpty()) {
            Map<String, List<WordBase>> grouped = new LinkedHashMap<>();
            grouped.put(studyPurpose, stageResults);
            result.stageResults = grouped;
            result.found = true;

            // 提取音标
            String ph = stageResults.get(0).getPhonetic();
            if (ph != null && !ph.isBlank()) {
                result.phonetic = ph;
            }
            return result;
        }

        // 2) 当前阶段未命中 → 按顺序遍历其他词书，返回第一个命中的词书
        // 词书顺序：初中 → 高中 → 四级 → 六级 → 考研 → 托福
        String[] ALL_STAGES = {"初中", "高中", "四级", "六级", "考研", "托福"};
        for (String stage : ALL_STAGES) {
            if (stage.equals(studyPurpose)) continue;  // 跳过已查过的当前阶段

            List<WordBase> fallbackResults = wordDAO.findByWordAndStage(word, stage);
            if (!fallbackResults.isEmpty()) {
                Map<String, List<WordBase>> grouped = new LinkedHashMap<>();
                grouped.put(stage, fallbackResults);
                result.stageResults = grouped;
                result.found = true;
                result.crossStage = true;

                String ph = fallbackResults.get(0).getPhonetic();
                if (ph != null && !ph.isBlank()) {
                    result.phonetic = ph;
                }
                return result;
            }
        }

        // 3) 词书均未命中 → 查询 AI 动态词典 ai_word_dic
        AiWordDic aiDic = wordDAO.findLatestAiDic(word);
        if (aiDic != null) {
            Map<String, List<WordBase>> grouped = new LinkedHashMap<>();
            // 将 ai_word_dic 结果伪装为词书结果
            WordBase wb = new WordBase();
            wb.setId(aiDic.getAidicId() != null ? aiDic.getAidicId() : 0L);
            wb.setWord(aiDic.getWord());
            wb.setPhonetic(aiDic.getPhonetic());
            wb.setTranslation(aiDic.getTranslation());
            wb.setExplanation(aiDic.getExplanation());
            grouped.put("AI 词典", java.util.Collections.singletonList(wb));
            result.stageResults = grouped;
            result.found = true;
            result.crossStage = true;  // 非当前阶段来源
            if (aiDic.getPhonetic() != null && !aiDic.getPhonetic().isBlank()) {
                result.phonetic = aiDic.getPhonetic();
            }
            return result;
        }

        // 4) ai_word_dic 也未命中 → AI 介入搜索并写入数据库
        Service.AIClient aiClient = new Service.AIClient();
        Service.AIWordService aiWordService = new Service.AIWordService(aiClient);
        Service.AIWordService.AiWordLookupResult aiResult = aiWordService.lookupWordByAI(word);
        if (aiResult.translation != null && !aiResult.translation.isBlank()) {
            // 写入 ai_word_dic 表
            AiWordDic newAiDic = new AiWordDic();
            newAiDic.setWord(word);
            newAiDic.setPhonetic(aiResult.phonetic);
            newAiDic.setTranslation(aiResult.translation);
            newAiDic.setExplanation(aiResult.explanation);
            newAiDic.setLikeCount(0);
            newAiDic.setDislikeCount(0);
            newAiDic.setIsRemoved(false);
            newAiDic.setCreatedAt(java.time.LocalDateTime.now());
            wordDAO.insertAiDic(newAiDic);

            // 返回结果
            Map<String, List<WordBase>> grouped = new LinkedHashMap<>();
            WordBase wb = new WordBase();
            wb.setId(0L);
            wb.setWord(word);
            wb.setPhonetic(aiResult.phonetic);
            wb.setTranslation(aiResult.translation);
            wb.setExplanation(aiResult.explanation);
            grouped.put("AI 生成", java.util.Collections.singletonList(wb));
            result.stageResults = grouped;
            result.found = true;
            result.crossStage = true;
            if (aiResult.phonetic != null && !aiResult.phonetic.isBlank()) {
                result.phonetic = aiResult.phonetic;
            }
            return result;
        }

        // 全部手段均未命中
        result.stageResults = new LinkedHashMap<>();
        result.found = false;

        return result;
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
                return "{\"success\":false,\"message\":" + JsonUtil.strVal(error) + "}";
            }
            // Gson 直接序列化类中的 public 字段，嵌套的 WordBase/AiWordDic 需要手动构建
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("success", true);
            obj.addProperty("word", word);
            obj.addProperty("studyPurpose", studyPurpose);
            obj.addProperty("foundInStage", foundInStage);
            obj.addProperty("isBeyondStage", isBeyondStage);
            obj.addProperty("multiMeaning", multiMeaning);

            com.google.gson.JsonArray stageArr = new com.google.gson.JsonArray();
            if (stageResults != null) {
                for (WordBase w : stageResults) {
                    com.google.gson.JsonObject wObj = new com.google.gson.JsonObject();
                    wObj.addProperty("id", w.getId());
                    wObj.addProperty("word", w.getWord());
                    wObj.addProperty("phonetic", w.getPhonetic());
                    wObj.addProperty("translation", w.getTranslation());
                    stageArr.add(wObj);
                }
            }
            obj.add("stageResults", stageArr);
            obj.addProperty("foundInAiDic", foundInAiDic);
            if (aiResult != null) {
                com.google.gson.JsonObject aiObj = new com.google.gson.JsonObject();
                aiObj.addProperty("aidicId", aiResult.getAidicId());
                aiObj.addProperty("word", aiResult.getWord());
                aiObj.addProperty("phonetic", aiResult.getPhonetic());
                aiObj.addProperty("translation", aiResult.getTranslation());
                aiObj.addProperty("explanation", aiResult.getExplanation());
                aiObj.addProperty("likeCount", aiResult.getLikeCount());
                aiObj.addProperty("dislikeCount", aiResult.getDislikeCount());
                obj.add("aiResult", aiObj);
            }
            return GsonUtil.GSON.toJson(obj);
        }
    }

    /** 全词库查词结果封装 */
    public static class WordLookupResult {
        public String word;
        public String phonetic;
        public Map<String, List<WordBase>> stageResults;
        public boolean found;
        public boolean crossStage;   // 是否来自跨词书检索（当前阶段未命中后回退）
        public String error;

        public String toJson() {
            if (error != null) {
                return "{\"success\":false,\"message\":" + JsonUtil.strVal(error) + "}";
            }
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("success", true);
            obj.addProperty("word", word);
            if (phonetic != null && !phonetic.isBlank()) {
                obj.addProperty("phonetic", phonetic);
            }
            obj.addProperty("found", found);
            obj.addProperty("crossStage", crossStage);

            com.google.gson.JsonArray results = new com.google.gson.JsonArray();
            if (stageResults != null) {
                for (Map.Entry<String, List<WordBase>> entry : stageResults.entrySet()) {
                    com.google.gson.JsonObject stageObj = new com.google.gson.JsonObject();
                    stageObj.addProperty("source", entry.getKey());
                    com.google.gson.JsonArray entries = new com.google.gson.JsonArray();
                    for (WordBase w : entry.getValue()) {
                        com.google.gson.JsonObject wObj = new com.google.gson.JsonObject();
                        wObj.addProperty("id", w.getId());
                        wObj.addProperty("word", w.getWord());
                        wObj.addProperty("phonetic", w.getPhonetic());
                        wObj.addProperty("translation", w.getTranslation());
                        if (w.getExplanation() != null && !w.getExplanation().isBlank()) {
                            wObj.addProperty("explanation", w.getExplanation());
                        }
                        entries.add(wObj);
                    }
                    stageObj.add("entries", entries);
                    results.add(stageObj);
                }
            }
            obj.add("results", results);
            return GsonUtil.GSON.toJson(obj);
        }

    }
}
