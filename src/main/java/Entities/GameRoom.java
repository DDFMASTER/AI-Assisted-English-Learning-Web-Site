package Entities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PK 游戏房间实体。
 * 每个房间有唯一 roomCode，包含房主和加入者信息、游戏状态、题目和双方答案。
 */
public class GameRoom {

    // ==== 状态常量 ====
    public static final String WAITING  = "WAITING";
    public static final String PLAYING  = "PLAYING";
    public static final String FINISHED = "FINISHED";

    /** 题目总数 */
    public static final int TOTAL_QUESTIONS = 20;

    // ==== 基础字段 ====
    private final String  roomCode;
    private final Long    hostUserId;
    private       Long    guestUserId;
    private       String  status;
    private       String  difficulty;
    private final long    createdAt;
    private       boolean isPublic;          // 公开 / 私密

    // ==== 游戏数据 ====
    /** 服务器端生成的题目列表 */
    private List<PkQuestion> questions;

    /** 房主答案: questionIndex → 是否正确 */
    private final Map<Integer, Boolean> hostAnswers;
    /** 加入者答案: questionIndex → 是否正确 */
    private final Map<Integer, Boolean> guestAnswers;

    /** 每题耗时(毫秒): questionIndex → elapsedMs */
    private final Map<Integer, Long> hostAnswerTimes;
    private final Map<Integer, Long> guestAnswerTimes;

    /** 是否已完成全部答题（用于等待对方） */
    private volatile boolean hostFinished;
    private volatile boolean guestFinished;

    /** 房间结束时间（毫秒时间戳），用于 FINISHED 房间的过期清理。0 表示未结束 */
    private volatile long finishedAt;

    // ==== 构造函数 ====
    public GameRoom(String roomCode, Long hostUserId, boolean isPublic) {
        this.roomCode   = roomCode;
        this.hostUserId = hostUserId;
        this.guestUserId = null;
        this.status     = WAITING;
        this.difficulty = "cet4";
        this.isPublic   = isPublic;
        this.createdAt  = System.currentTimeMillis();
        this.questions  = new ArrayList<>();
        this.hostAnswers      = new ConcurrentHashMap<>();
        this.guestAnswers     = new ConcurrentHashMap<>();
        this.hostAnswerTimes  = new ConcurrentHashMap<>();
        this.guestAnswerTimes = new ConcurrentHashMap<>();
    }

    // ==== Getter ====
    public String  getRoomCode()     { return roomCode; }
    public Long    getHostUserId()   { return hostUserId; }
    public Long    getGuestUserId()  { return guestUserId; }
    public String  getStatus()       { return status; }
    public String  getDifficulty()   { return difficulty; }
    public long    getCreatedAt()    { return createdAt; }
    public boolean isPublic()        { return isPublic; }
    public List<PkQuestion> getQuestions()            { return questions; }
    public Map<Integer, Boolean> getHostAnswers()     { return hostAnswers; }
    public Map<Integer, Boolean> getGuestAnswers()    { return guestAnswers; }
    public Map<Integer, Long>  getHostAnswerTimes()   { return hostAnswerTimes; }
    public Map<Integer, Long>  getGuestAnswerTimes()  { return guestAnswerTimes; }
    public boolean isHostFinished()   { return hostFinished; }
    public boolean isGuestFinished()  { return guestFinished; }

    public boolean isReady()  { return hostUserId != null && guestUserId != null; }
    public boolean isHost(Long userId)  { return hostUserId != null && hostUserId.equals(userId); }
    public boolean isGuest(Long userId) { return guestUserId != null && guestUserId.equals(userId); }

    // ==== Setter ====
    public void setGuestUserId(Long guestUserId) { this.guestUserId = guestUserId; }
    public void setStatus(String status)         { this.status = status; }
    public long   getFinishedAt()   { return finishedAt; }

    /** 将房间标记为已结束并记录时间，供后续过期清理使用 */
    public void finish() {
        this.status = FINISHED;
        this.finishedAt = System.currentTimeMillis();
    }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestions(List<PkQuestion> questions) { this.questions = questions; }
    public void setPublic(boolean isPublic)      { this.isPublic = isPublic; }

    // ==== 答案记录 ====
    /** 记录答案及耗时 */
    public void recordAnswer(Long userId, int questionIndex, boolean correct, long elapsedMs) {
        if (isHost(userId)) {
            hostAnswers.put(questionIndex, correct);
            hostAnswerTimes.put(questionIndex, elapsedMs);
        } else if (isGuest(userId)) {
            guestAnswers.put(questionIndex, correct);
            guestAnswerTimes.put(questionIndex, elapsedMs);
        }
    }

    /** 标记某用户已完成全部答题 */
    public void markFinished(Long userId) {
        if (isHost(userId)) {
            hostFinished = true;
        } else if (isGuest(userId)) {
            guestFinished = true;
        }
    }

    /** 获取某用户正确答案数 */
    public int getCorrectCount(Long userId) {
        Map<Integer, Boolean> answers = isHost(userId) ? hostAnswers : guestAnswers;
        return (int) answers.values().stream().filter(Boolean::booleanValue).count();
    }

    /** 获取某用户已答题数 */
    public int getAnsweredCount(Long userId) {
        Map<Integer, Boolean> answers = isHost(userId) ? hostAnswers : guestAnswers;
        return answers.size();
    }

    /** 获取某用户总耗时(毫秒) */
    public long getTotalTime(Long userId) {
        Map<Integer, Long> times = isHost(userId) ? hostAnswerTimes : guestAnswerTimes;
        return times.values().stream().mapToLong(Long::longValue).sum();
    }

    /** 双方是否都已答完 */
    public boolean bothFinished() {
        return hostFinished && guestFinished;
    }

    // ============================================
    // 内部类：PK 题目
    // ============================================
    public static class PkQuestion {
        private String       word;
        private List<String> options;
        private int          correctIndex;

        public PkQuestion() {}

        public PkQuestion(String word, List<String> options, int correctIndex) {
            this.word         = word;
            this.options      = options;
            this.correctIndex = correctIndex;
        }

        public String       getWord()         { return word; }
        public List<String> getOptions()      { return options; }
        public int          getCorrectIndex() { return correctIndex; }

        public void setWord(String word)                { this.word = word; }
        public void setOptions(List<String> options)     { this.options = options; }
        public void setCorrectIndex(int correctIndex)    { this.correctIndex = correctIndex; }
    }
}
