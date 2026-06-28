package Servlet;

import Service.WordService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/word/search")
public class WordSearchServlet extends HttpServlet {
    private final WordService wordService = new WordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String word = request.getParameter("word");
        String studyPurpose = request.getParameter("studyPurpose");

        // 默认阶段为四级
        if (studyPurpose == null || studyPurpose.isBlank()) {
            studyPurpose = "四级";
        }

        WordService.WordSearchResult result = wordService.searchWord(word, studyPurpose);
        response.getWriter().write(result.toJson());
    }
}
