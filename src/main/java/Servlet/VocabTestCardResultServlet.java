package Servlet;

import DAO.UserDAOImpl;
import Service.VocabTestCardService;
import Service.VocabTestCardService.CardTestResult;
import Service.VocabTestCardService.CardWord;
import Service.VocabTestCardService.WordOptions;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 词汇量卡片测试 — 接收前端统计结果
 * POST /api/vocabtest/cardresult
 * Body: { userId, correct, total, realCorrect, realTotal, pseudoCorrect, pseudoTotal, estimatedVocab, cefrLevel }
 *
 * 前端已实时统计正确数，后端仅负责更新用户 literacy。
 */
@WebServlet("/api/vocabtest/cardresult")
public class VocabTestCardResultServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        StringBuilder bodyBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
        }
        String body = bodyBuilder.toString();

        Long userId = extractLong(body, "userId");
        int estimatedVocab = extractInt(body, "estimatedVocab");

        if (userId == null) {
            response.getWriter().write(JsonUtil.error("缺少 userId"));
            return;
        }

        // 清除 session 中的词表
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("vocabTestCardWords");
        }

        // 更新用户词汇量
        if (estimatedVocab > 0) {
            new UserDAOImpl().updateLiteracy(userId, estimatedVocab);
        }

        response.getWriter().write(JsonUtil.success("结果已保存"));
    }

    private Long extractLong(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = start;
        while (end < json.length() && json.charAt(end) >= '0' && json.charAt(end) <= '9') end++;
        try { return Long.parseLong(json.substring(start, end)); } catch (NumberFormatException e) { return null; }
    }

    private int extractInt(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return 0;
        int colon = json.indexOf(":", idx);
        if (colon < 0) return 0;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = start;
        while (end < json.length() && json.charAt(end) >= '0' && json.charAt(end) <= '9') end++;
        try { return Integer.parseInt(json.substring(start, end)); } catch (NumberFormatException e) { return 0; }
    }
}
