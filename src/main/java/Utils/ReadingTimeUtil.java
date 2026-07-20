package Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 阅读时间估算工具 — 根据文章长度、难度和用户学习阶段计算预计阅读时长。
 *
 * 算法要点：
 *   1. 词数估算：contentLength / 5（英语平均每词约 5 字符，含空格）
 *   2. 基准阅读速度（wpm）：基于用户学习阶段（初中 80 → 托福 180）
 *   3. 难度修正：文章难度与用户水平每差一级 ±15%，钳制在 0.4~1.6
 *   4. 结果精确到 0.5 分钟，最小 0.5 分钟
 */
public class ReadingTimeUtil {

    /** 默认阅读速度（词/分钟），无用户上下文时使用 */
    private static final double DEFAULT_WPM = 120.0;

    /** 平均每词字符数（含空格），用于从 content char length 估算词数 */
    private static final double CHARS_PER_WORD = 5.0;

    /** 难度等级映射 */
    private static int difficultyLevel(String difficulty) {
        if (difficulty == null) return 4; // 默认按"六级"处理
        return switch (difficulty) {
            case "初中" -> 1;
            case "高中" -> 2;
            case "四级" -> 3;
            case "六级" -> 4;
            case "考研" -> 5;
            case "托福" -> 6;
            default    -> 7; // 期刊、原著、网络新闻等 → 最难
        };
    }

    /** 用户学习阶段 → 基准阅读速度（wpm） */
    private static double baseWpm(String studyPurpose) {
        if (studyPurpose == null) return DEFAULT_WPM;
        return switch (studyPurpose) {
            case "初中" ->  80.0;
            case "高中" -> 100.0;
            case "四级" -> 120.0;
            case "六级" -> 140.0;
            case "考研" -> 160.0;
            case "托福" -> 180.0;
            default     -> DEFAULT_WPM;
        };
    }

    /**
     * 计算阅读时间（分钟），精确到 0.5。
     *
     * @param contentLength 文章正文 char 长度
     * @param articleDifficulty 文章难度标签
     * @param studyPurpose 用户学习阶段，可为 null
     * @return 阅读时间（分钟），如 1.0, 2.5, 3.0
     */
    public static double calculate(int contentLength, String articleDifficulty,
                                   String studyPurpose) {
        // 1. 词数估算
        double wordCount = Math.max(1.0, contentLength / CHARS_PER_WORD);

        // 2. 基准 WPM
        double wpm = baseWpm(studyPurpose);

        // 3. 难度修正
        int userLevel  = studyPurpose != null ? difficultyLevel(studyPurpose) : 4;
        int articleLevel = difficultyLevel(articleDifficulty);
        int levelDiff = userLevel - articleLevel;  // 正=文章更简单, 负=文章更难
        double factor = 1.0 + levelDiff * 0.15;
        factor = Math.max(0.4, Math.min(1.6, factor));  // 钳制

        double adjustedWpm = wpm * factor;

        // 4. 计算并精确到 0.5 分钟
        double minutes = wordCount / adjustedWpm;
        double rounded = Math.round(minutes * 2.0) / 2.0;
        return Math.max(0.5, rounded);
    }

    /**
     * 便捷方法：从 HttpServletRequest 中尝试获取用户学习阶段。
     *
     * @param request HTTP 请求
     * @return 用户学习阶段，未登录返回 null
     */
    public static String getStudyPurpose(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("studyPurpose");
    }
}
