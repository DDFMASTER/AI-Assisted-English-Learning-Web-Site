package Servlet;

import Service.VocabTestService;
import Service.VocabTestService.TestWord;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 词汇量初次检测 — 获取测试单词（COCA 词频段抽样）
 * GET /api/vocabtest/words           → 返回初始 100 个测试词
 * GET /api/vocabtest/words?more=10   → 追加 10 个测试词
 *
 * 已用词 tracking 存储在 session 中，每个用户独立。
 */
@WebServlet("/api/vocabtest/words")
public class VocabTestWordsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 禁止浏览器/代理缓存，确保每次调用都获取全新的随机单词
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        HttpSession session = request.getSession(true);
        VocabTestService service = VocabTestService.getInstance();

        String moreParam = request.getParameter("more");
        List<TestWord> newWords;

        if (moreParam != null) {
            // 追加更多单词
            @SuppressWarnings("unchecked")
            Set<String> usedReal = (Set<String>) session.getAttribute("vocabTestUsedReal");
            @SuppressWarnings("unchecked")
            Set<String> usedFake = (Set<String>) session.getAttribute("vocabTestUsedFake");
            if (usedReal == null) usedReal = new HashSet<>();
            if (usedFake == null) usedFake = new HashSet<>();

            newWords = service.sampleMore(usedReal, usedFake);

            session.setAttribute("vocabTestUsedReal", usedReal);
            session.setAttribute("vocabTestUsedFake", usedFake);
        } else {
            // 初始抽样 — 全新测试，清空 session 中的所有旧状态
            newWords = service.sampleInitial();
            // 从初始词中提取已用集合存入 session
            Set<String> usedReal = new HashSet<>();
            Set<String> usedFake = new HashSet<>();
            for (TestWord tw : newWords) {
                if (tw.isFake) usedFake.add(tw.word);
                else usedReal.add(tw.word);
            }
            session.setAttribute("vocabTestUsedReal", usedReal);
            session.setAttribute("vocabTestUsedFake", usedFake);
            // 新测试开始，重置总词表（之前的旧数据不应影响本次测试）
            session.setAttribute("vocabTestWords", new ArrayList<>(newWords));
        }

        // 追加到 session 中的总词表（仅在 more 模式时执行）
        @SuppressWarnings("unchecked")
        List<TestWord> allWords = (List<TestWord>) session.getAttribute("vocabTestWords");
        if (allWords == null) allWords = new ArrayList<>();
        if (moreParam != null) {
            allWords.addAll(newWords);
            session.setAttribute("vocabTestWords", allWords);
        }

        // 构建响应
        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"words\":[");
        for (int i = 0; i < newWords.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{\"word\":").append(JsonUtil.strVal(newWords.get(i).word))
                .append(",\"index\":").append(allWords.size() - newWords.size() + i)
                .append(",\"isFake\":").append(newWords.get(i).isFake)
                .append("}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }
}
