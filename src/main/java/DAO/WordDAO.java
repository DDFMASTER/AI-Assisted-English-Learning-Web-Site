package DAO;

import Entities.AiWordDic;
import Entities.WordBase;

import java.util.List;

public interface WordDAO {
    /**
     * 根据单词和学习阶段查询对应词库表
     * @param word 单词
     * @param stage 学习阶段：初中/高中/四级/六级/考研/托福
     * @return 该单词在该阶段词库中的所有释义列表
     */
    List<WordBase> findByWordAndStage(String word, String stage);

    /**
     * 从 AI 动态词典查询单词最新的未删除解释
     */
    AiWordDic findLatestAiDic(String word);

    /**
     * 插入 AI 动态词典条目
     */
    int insertAiDic(AiWordDic aiWordDic);
}
