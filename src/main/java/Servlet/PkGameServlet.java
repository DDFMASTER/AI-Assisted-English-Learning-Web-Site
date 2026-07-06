package Servlet;

import Entities.GameRoom;
import Service.GameRoomManager;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * PK 游戏对战接口。
 *
 * POST /api/pk/game
 * Body: { "action": "start|answer|opponent|quit|questions|result",
 *         "roomCode": "1234", "difficulty": "cet4",
 *         "questionIndex": 0, "correct": true, "elapsedMs": 3500 }
 */
@WebServlet("/api/pk/game")
public class PkGameServlet extends HttpServlet {

    private final GameRoomManager roomManager = new GameRoomManager();

    // ==== 词库 (每个难度至少20词) ====
    private static final Map<String, List<WordEntry>> WORD_BANK = new LinkedHashMap<>();
    static {
        WORD_BANK.put("junior", List.of(
            we("abandon", "放弃,获得,开始,结束", 0),
            we("ability", "能力,残疾,可能,责任", 0),
            we("abroad", "国内,国外,船上,车上", 1),
            we("accept", "拒绝,接受,发送,等待", 1),
            we("accident", "意图,计划,事故,成功", 2),
            we("achieve", "失去,达到,开始,停止", 1),
            we("active", "消极的,积极的,安静的,快速的", 1),
            we("actual", "虚拟的,实际的,理论的,假设的", 1),
            we("admire", "厌恶,羡慕,忽视,批评", 1),
            we("adventure", "冒险,安全,平凡,休息", 0),
            we("ancient", "古代的,现代的,未来的,年轻的", 0),
            we("balance", "失衡,平衡,天平,重量", 1),
            we("celebrate", "忽视,庆祝,批评,破坏", 1),
            we("dangerous", "安全的,危险的,稳定的,友好的", 1),
            we("defense", "攻击,防御,撤退,伪装", 1),
            we("enormous", "微小的,巨大的,普通的,常见的", 1),
            we("frequent", "罕见的,频繁的,偶尔的,一次性的", 1),
            we("generous", "吝啬的,慷慨的,自私的,贪婪的", 1),
            we("identify", "隐藏,识别,混淆,忽视", 1),
            we("journey", "停留,旅行,终点,起点", 1)
        ));
        WORD_BANK.put("senior", List.of(
            we("abundant", "稀缺的,丰富的,普通的,空的", 1),
            we("barrier", "帮助,障碍,桥梁,通道", 1),
            we("controversy", "协议,争论,沉默,庆祝", 1),
            we("democracy", "独裁,民主,封建,君主", 1),
            we("erupt", "平息,爆发,隐藏,建造", 1),
            we("flourish", "枯萎,繁荣,挣扎,忽视", 1),
            we("genuine", "伪造的,真正的,普通的,临时的", 1),
            we("humble", "傲慢的,谦虚的,富有的,快速的", 1),
            we("inevitable", "可避免的,不可避免的,随机的,罕见的", 1),
            we("justice", "不公,正义,法律,犯罪", 1),
            we("keen", "迟钝的,敏锐的,懒惰的,模糊的", 1),
            we("liberty", "束缚,自由,奴役,控制", 1),
            we("magnificent", "平凡的,壮丽的,丑陋的,简单的", 1),
            we("notion", "事实,概念,实体,证据", 1),
            we("obstacle", "帮助,障碍,捷径,通道", 1),
            we("peculiar", "普通的,奇特的,常见的,标准的", 1),
            we("reluctant", "渴望的,不情愿的,热情的,主动的", 1),
            we("splendid", "糟糕的,辉煌的,普通的,阴暗的", 1),
            we("triumph", "失败,胜利,挫折,退步", 1),
            we("vivid", "模糊的,生动的,暗淡的,乏味的", 1)
        ));
        WORD_BANK.put("cet4", List.of(
            we("abandon", "放弃,获得,开始,结束", 0),
            we("elaborate", "简略的,精心制作的,模糊的,过时的", 1),
            we("fluctuate", "稳定,波动,上升,下降", 1),
            we("genuine", "伪造的,真正的,临时的,昂贵的", 1),
            we("hypothesis", "结论,事实,假设,证据", 2),
            we("inevitable", "可避免的,不可避免的,随机的,罕见的", 1),
            we("jeopardize", "保护,危及,改善,忽视", 1),
            we("negotiate", "拒绝,接受,谈判,发送", 2),
            we("perspective", "图片,计划,观点,比例", 2),
            we("substantial", "微小的,大量的,临时的,表面的", 1),
            we("accommodate", "拒绝,容纳,驱逐,忽视", 1),
            we("demonstrate", "隐藏,展示,混淆,破坏", 1),
            we("enthusiasm", "冷漠,热情,犹豫,恐惧", 1),
            we("fundamental", "表面的,基础的,复杂的,次要的", 1),
            we("implement", "废除,实施,忽视,延迟", 1),
            we("phenomenon", "事实,现象,理论,假设", 1),
            we("reluctant", "渴望的,不情愿的,热情的,主动的", 1),
            we("sophisticated", "简单的,复杂的,原始的,粗糙的", 1),
            we("tremendous", "微小的,巨大的,普通的,平凡的", 1),
            we("vulnerable", "坚强的,脆弱的,强大的,安全的", 1)
        ));
        WORD_BANK.put("cet6", List.of(
            we("ambiguous", "清晰的,模糊的,简短的,冗长的", 1),
            we("benevolent", "恶意的,仁慈的,冷漠的,贪婪的", 1),
            we("conscientious", "粗心的,认真的,懒惰的,傲慢的", 1),
            we("deteriorate", "改善,恶化,稳定,波动", 1),
            we("extravagant", "节俭的,奢侈的,普通的,简朴的", 1),
            we("formidable", "容易的,可怕的,微小的,普通的", 1),
            we("gregarious", "孤僻的,群居的,安静的,害羞的", 1),
            we("homogeneous", "多样的,同质的,异质的,混合的", 1),
            we("indigenous", "外来的,本土的,进口的,出口的", 1),
            we("meticulous", "粗心的,一丝不苟的,懒惰的,随意的", 1),
            we("notorious", "无名的,臭名昭著的,著名的,匿名的", 1),
            we("obsolete", "现代的,过时的,先进的,永恒的", 1),
            we("predominant", "次要的,主要的,微小的,隐藏的", 1),
            we("reconcile", "分离,调和,冲突,忽视", 1),
            we("scrutiny", "忽视,审查,信任,粗略", 1),
            we("trivial", "重要的,琐碎的,复杂的,深奥的", 1),
            we("undermine", "加强,削弱,支持,建立", 1),
            we("versatile", "单一的,多才多艺的,笨拙的,局限的", 1),
            we("warrant", "否认,保证,拒绝,忽视", 1),
            we("yield", "抵抗,屈服,坚持,攻击", 1)
        ));
        WORD_BANK.put("graduate", List.of(
            we("apprehension", "理解,忧虑,欣赏,应用", 1),
            we("bureaucracy", "民主制度,官僚机构,商业体系,教育系统", 1),
            we("commodity", "社区,商品,公司,委员会", 1),
            we("dilemma", "解决方案,困境,对话,图表", 1),
            we("ephemeral", "永恒的,短暂的,巨大的,微小的", 1),
            we("facetious", "严肃的,滑稽的,表面的,多面的", 1),
            we("gratuitous", "感激的,无理由的,免费的,昂贵的", 1),
            we("hegemony", "平等,霸权,混乱,和谐", 1),
            we("impeccable", "有瑕疵的,无瑕疵的,可修复的,可改变的", 1),
            we("juxtapose", "分离,并列,移除,插入", 1),
            we("kaleidoscope", "单色,万花筒,望远镜,显微镜", 1),
            we("lethargic", "活跃的,昏睡的,激动的,快速的", 1),
            we("meticulous", "粗心的,一丝不苟的,懒惰的,随意的", 1),
            we("nostalgia", "恐惧,怀旧,期待,厌恶", 1),
            we("ostentatious", "朴素的,炫耀的,隐藏的,谦虚的", 1),
            we("precarious", "稳定的,危险的,安全的,确定的", 1),
            we("quintessential", "次要的,精髓的,表面的,外来的", 1),
            we("resilient", "脆弱的,有弹性的,僵硬的,软弱的", 1),
            we("surreptitious", "公开的,秘密的,明显的,直接的", 1),
            we("ubiquitous", "罕见的,无处不在的,局部的,有限的", 1)
        ));
        WORD_BANK.put("toefl", List.of(
            we("aberration", "正常,异常,平均值,标准", 1),
            we("bellicose", "和平的,好战的,友好的,安静的", 1),
            we("cacophony", "和谐的声音,刺耳的声音,沉默,回声", 1),
            we("deleterious", "有益的,有害的,中性的,美味的", 1),
            we("ephemeral", "永恒的,短暂的,巨大的,微小的", 1),
            we("fastidious", "随意的,挑剔的,快速的,坚定的", 1),
            we("garrulous", "沉默的,唠叨的,勇敢的,慷慨的", 1),
            we("iconoclast", "传统拥护者,传统反叛者,偶像崇拜者,艺术家", 1),
            we("laconic", "冗长的,简洁的,抒情的,逻辑的", 1),
            we("munificent", "吝啬的,慷慨的,邪恶的,平凡的", 1),
            we("nefarious", "善良的,邪恶的,中立的,友好的", 1),
            we("obsequious", "独立的,奉承的,对抗的,忽视的", 1),
            we("perfunctory", "彻底的,敷衍的,热情的,仔细的", 1),
            we("quagmire", "高地,困境,平坦,成功", 1),
            we("recalcitrant", "顺从的,反抗的,合作的,被动的", 1),
            we("sycophant", "领导者,马屁精,敌对者,创造者", 1),
            we("taciturn", "健谈的,沉默寡言的,活跃的,喧闹的", 1),
            we("unctuous", "真诚的,油滑的,干燥的,直率的", 1),
            we("vociferous", "安静的,喧嚷的,沉默的,含蓄的", 1),
            we("zealous", "冷漠的,热情的,懒惰的,消极的", 1)
        ));
    }

    // helper: 构建 WordEntry
    private static WordEntry we(String word, String optsCsv, int correct) {
        return new WordEntry(word, Arrays.asList(optsCsv.split(",")), correct);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = ServletUtil.getSessionUserId(request);
        if (userId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        String body = readBody(request);
        String action = extractField(body, "action");
        String roomCode = extractField(body, "roomCode");

        if (action == null) action = "opponent";

        String result;
        switch (action) {
            case "start"     -> result = handleStart(request, userId, roomCode, body);
            case "answer"    -> result = handleAnswer(request, userId, roomCode, body);
            case "opponent"  -> result = handleOpponent(request, userId, roomCode);
            case "quit"      -> result = handleQuit(request, userId, roomCode);
            case "questions" -> result = handleQuestions(request, userId, roomCode);
            case "result"    -> result = handleResult(request, userId, roomCode);
            default          -> result = JsonUtil.error("未知操作: " + action);
        }

        response.getWriter().write(result);
    }

    // ============================================
    // 开始游戏（仅房主）
    // ============================================
    private String handleStart(HttpServletRequest request, Long userId,
                                String roomCode, String body) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");
        if (!room.isHost(userId)) return JsonUtil.error("只有房主可以开始游戏");
        if (!room.isReady()) return JsonUtil.error("等待另一位玩家加入");

        String difficulty = extractField(body, "difficulty");
        if (difficulty == null || difficulty.isBlank()) difficulty = "cet4";
        room.setDifficulty(difficulty);

        List<GameRoom.PkQuestion> questions = generateQuestions(difficulty);
        room.setQuestions(questions);
        room.setStatus(GameRoom.PLAYING);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true");
        sb.append(",\"roomCode\":\"").append(roomCode.trim()).append("\"");
        sb.append(",\"totalQuestions\":").append(questions.size());
        sb.append(",\"questions\":[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) sb.append(",");
            GameRoom.PkQuestion q = questions.get(i);
            sb.append("{");
            sb.append("\"index\":").append(i).append(",");
            sb.append("\"word\":\"").append(JsonUtil.escapeJson(q.getWord())).append("\",");
            sb.append("\"correctIndex\":").append(q.getCorrectIndex()).append(",");
            sb.append("\"options\":[");
            for (int j = 0; j < q.getOptions().size(); j++) {
                if (j > 0) sb.append(",");
                sb.append("\"").append(JsonUtil.escapeJson(q.getOptions().get(j))).append("\"");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    // ============================================
    // 提交答案（含耗时，服务器校验正确性）
    // ============================================
    private String handleAnswer(HttpServletRequest request, Long userId,
                                 String roomCode, String body) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");
        if (!GameRoom.PLAYING.equals(room.getStatus()))
            return JsonUtil.error("游戏未在进行中");

        int questionIndex = extractIntField(body, "questionIndex", -1);
        int selectedOption = extractIntField(body, "selectedOption", -1);
        long elapsedMs = extractLongField(body, "elapsedMs", 0);

        // 服务器端校验答案
        boolean correct = false;
        if (selectedOption >= 0 && questionIndex >= 0 && questionIndex < room.getQuestions().size()) {
            GameRoom.PkQuestion q = room.getQuestions().get(questionIndex);
            correct = (selectedOption == q.getCorrectIndex());
        }

        if (questionIndex < 0) return JsonUtil.error("缺少 questionIndex");

        room.recordAnswer(userId, questionIndex, correct, elapsedMs);

        // 检查是否全部答完
        int total = room.getQuestions().size();
        if (room.getAnsweredCount(userId) >= total) {
            room.markFinished(userId);
        }

        // 判断胜负结果
        boolean hostWin = false, guestWin = false, tie = false;
        if (room.bothFinished()) {
            int hc = room.getCorrectCount(room.getHostUserId());
            int gc = room.getCorrectCount(room.getGuestUserId() != null ? room.getGuestUserId() : 0L);
            long ht = room.getTotalTime(room.getHostUserId());
            long gt = room.getTotalTime(room.getGuestUserId() != null ? room.getGuestUserId() : 0L);
            if (hc > gc) hostWin = true;
            else if (gc > hc) guestWin = true;
            else if (ht < gt) hostWin = true;
            else if (gt < ht) guestWin = true;
            else tie = true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"recorded\":true");
        sb.append(",\"myFinished\":").append(room.isHost(userId) ? room.isHostFinished() : room.isGuestFinished());
        sb.append(",\"bothFinished\":").append(room.bothFinished());
        sb.append(",\"hostWin\":").append(hostWin);
        sb.append(",\"guestWin\":").append(guestWin);
        sb.append(",\"tie\":").append(tie);
        sb.append("}");
        return sb.toString();
    }

    // ============================================
    // 查询对手进度（含耗时）
    // ============================================
    private String handleOpponent(HttpServletRequest request, Long userId,
                                   String roomCode) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");

        Long opponentId = room.isHost(userId) ? room.getGuestUserId() : room.getHostUserId();
        int oppCorrect  = room.getCorrectCount(opponentId != null ? opponentId : 0L);
        int oppAnswered = room.getAnsweredCount(opponentId != null ? opponentId : 0L);
        long oppTime    = room.getTotalTime(opponentId != null ? opponentId : 0L);

        // 判断胜负
        String winner = null;
        boolean tie = false;
        if (room.bothFinished()) {
            int hc = room.getCorrectCount(room.getHostUserId());
            int gc = room.getCorrectCount(room.getGuestUserId() != null ? room.getGuestUserId() : 0L);
            long ht = room.getTotalTime(room.getHostUserId());
            long gt = room.getTotalTime(room.getGuestUserId() != null ? room.getGuestUserId() : 0L);
            if (hc > gc) winner = "host";
            else if (gc > hc) winner = "guest";
            else if (ht < gt) winner = "host";
            else if (gt < ht) winner = "guest";
            else tie = true;
        }

        return "{\"success\":true"
                + ",\"opponentCorrect\":" + oppCorrect
                + ",\"opponentAnswered\":" + oppAnswered
                + ",\"opponentTime\":" + (oppTime / 1000)
                + ",\"opponentFinished\":" + (opponentId != null && (room.isHost(opponentId) ? room.isHostFinished() : room.isGuestFinished()))
                + ",\"totalQuestions\":" + room.getQuestions().size()
                + ",\"myCorrect\":" + room.getCorrectCount(userId)
                + ",\"myAnswered\":" + room.getAnsweredCount(userId)
                + ",\"myTime\":" + (room.getTotalTime(userId) / 1000)
                + ",\"myFinished\":" + (room.isHost(userId) ? room.isHostFinished() : room.isGuestFinished())
                + ",\"bothFinished\":" + room.bothFinished()
                + ",\"status\":\"" + room.getStatus() + "\""
                + (winner != null ? ",\"winner\":\"" + winner + "\"" : "")
                + ",\"tie\":" + tie
                + "}";
    }

    // ============================================
    // 获取最终结果
    // ============================================
    private String handleResult(HttpServletRequest request, Long userId,
                                 String roomCode) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");

        boolean isHost = room.isHost(userId);
        Long hostId = room.getHostUserId();
        Long guestId = room.getGuestUserId();

        int hc = room.getCorrectCount(hostId != null ? hostId : 0L);
        int gc = room.getCorrectCount(guestId != null ? guestId : 0L);
        long ht = room.getTotalTime(hostId != null ? hostId : 0L);
        long gt = room.getTotalTime(guestId != null ? guestId : 0L);

        String winner = null;
        boolean tie = false;
        if (hc > gc) winner = "host";
        else if (gc > hc) winner = "guest";
        else if (ht < gt) winner = "host";
        else if (gt < ht) winner = "guest";
        else tie = true;

        boolean iWin = (isHost && "host".equals(winner)) || (!isHost && "guest".equals(winner));

        return "{\"success\":true"
                + ",\"myCorrect\":" + (isHost ? hc : gc)
                + ",\"opponentCorrect\":" + (isHost ? gc : hc)
                + ",\"myTime\":" + ((isHost ? ht : gt) / 1000)
                + ",\"opponentTime\":" + ((isHost ? gt : ht) / 1000)
                + ",\"totalQuestions\":" + room.getQuestions().size()
                + ",\"win\":" + iWin
                + ",\"tie\":" + tie
                + ",\"quitByOpponent\":" + (GameRoom.FINISHED.equals(room.getStatus())
                        && !room.bothFinished() && !room.isHostFinished() == isHost && !room.isGuestFinished() == isHost)
                + ",\"status\":\"" + room.getStatus() + "\""
                + "}";
    }

    // ============================================
    // 退出游戏
    // ============================================
    private String handleQuit(HttpServletRequest request, Long userId,
                               String roomCode) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");

        // 标记为 FINISHED，对手自动获胜
        room.setStatus(GameRoom.FINISHED);

        return "{\"success\":true,\"message\":\"已退出对战\"}";
    }

    // ============================================
    // 获取题目（guest 用）
    // ============================================
    private String handleQuestions(HttpServletRequest request, Long userId,
                                    String roomCode) {
        if (roomCode == null || roomCode.isBlank())
            return JsonUtil.error("缺少 roomCode");

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) return JsonUtil.error("房间不存在");
        if (!room.isHost(userId) && !room.isGuest(userId))
            return JsonUtil.error("你不是该房间的参与者");

        List<GameRoom.PkQuestion> questions = room.getQuestions();
        if (questions.isEmpty())
            return JsonUtil.error("题目尚未生成，请等待房主开始游戏");

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true");
        sb.append(",\"totalQuestions\":").append(questions.size());
        sb.append(",\"questions\":[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) sb.append(",");
            GameRoom.PkQuestion q = questions.get(i);
            sb.append("{");
            sb.append("\"index\":").append(i).append(",");
            sb.append("\"word\":\"").append(JsonUtil.escapeJson(q.getWord())).append("\",");
            sb.append("\"correctIndex\":").append(q.getCorrectIndex()).append(",");
            sb.append("\"options\":[");
            for (int j = 0; j < q.getOptions().size(); j++) {
                if (j > 0) sb.append(",");
                sb.append("\"").append(JsonUtil.escapeJson(q.getOptions().get(j))).append("\"");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    // ============================================
    // 题目生成 (20题)
    // ============================================
    private List<GameRoom.PkQuestion> generateQuestions(String difficulty) {
        List<WordEntry> bank = WORD_BANK.getOrDefault(difficulty, WORD_BANK.get("cet4"));
        List<WordEntry> pool = new ArrayList<>(bank);
        Collections.shuffle(pool);

        List<GameRoom.PkQuestion> questions = new ArrayList<>();
        int count = Math.min(GameRoom.TOTAL_QUESTIONS, pool.size());
        for (int i = 0; i < count; i++) {
            WordEntry entry = pool.get(i);
            List<String> shuffledOpts = new ArrayList<>(entry.options);
            String correctText = entry.options.get(entry.correctIndex);
            Collections.shuffle(shuffledOpts);
            int newCorrect = shuffledOpts.indexOf(correctText);
            questions.add(new GameRoom.PkQuestion(entry.word, shuffledOpts, newCorrect));
        }
        return questions;
    }

    // ============================================
    // JSON 辅助
    // ============================================
    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String extractField(String body, String fieldName) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":\"",
                "\"" + fieldName + "\": \"",
                "\"" + fieldName + "\" :\""
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                StringBuilder val = new StringBuilder();
                for (int i = start; i < body.length(); i++) {
                    char c = body.charAt(i);
                    if (c == '\\' && i + 1 < body.length()) {
                        char next = body.charAt(i + 1);
                        switch (next) {
                            case '"' -> { val.append('"'); i++; }
                            case '\\' -> { val.append('\\'); i++; }
                            case 'n' -> { val.append('\n'); i++; }
                            case 'r' -> { val.append('\r'); i++; }
                            case 't' -> { val.append('\t'); i++; }
                            default -> val.append(c);
                        }
                    } else if (c == '"') {
                        break;
                    } else {
                        val.append(c);
                    }
                }
                return val.toString();
            }
        }
        return null;
    }

    private int extractIntField(String body, String fieldName, int defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                StringBuilder num = new StringBuilder();
                while (start < body.length() && Character.isDigit(body.charAt(start))) {
                    num.append(body.charAt(start));
                    start++;
                }
                if (num.length() > 0) {
                    try { return Integer.parseInt(num.toString()); }
                    catch (NumberFormatException e) { return defaultVal; }
                }
            }
        }
        return defaultVal;
    }

    private long extractLongField(String body, String fieldName, long defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                StringBuilder num = new StringBuilder();
                while (start < body.length() && Character.isDigit(body.charAt(start))) {
                    num.append(body.charAt(start));
                    start++;
                }
                if (num.length() > 0) {
                    try { return Long.parseLong(num.toString()); }
                    catch (NumberFormatException e) { return defaultVal; }
                }
            }
        }
        return defaultVal;
    }

    private boolean extractBoolField(String body, String fieldName, boolean defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                if (body.substring(start).startsWith("true")) return true;
                if (body.substring(start).startsWith("false")) return false;
            }
        }
        return defaultVal;
    }

    // ============================================
    // 词条内部类
    // ============================================
    private static class WordEntry {
        final String       word;
        final List<String> options;
        final int          correctIndex;

        WordEntry(String word, List<String> options, int correctIndex) {
            this.word         = word;
            this.options      = options;
            this.correctIndex = correctIndex;
        }
    }
}
