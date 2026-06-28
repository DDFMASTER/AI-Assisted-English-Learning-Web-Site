package Servlet;

import Entities.Article;
import Service.ArticleService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/article/list")
public class ArticleListServlet extends HttpServlet {
    private final ArticleService articleService = new ArticleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String difficulty = request.getParameter("difficulty");

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"articles\":[");

        try {
            List<Article> articles = articleService.getArticleList(difficulty);
            boolean first = true;
            for (Article a : articles) {
                if (!first) json.append(",");
                first = false;

                json.append("{");
                json.append("\"articleId\":").append(a.getArticleId()).append(",");
                json.append("\"title\":\"").append(JsonUtil.escapeJson(a.getTitle())).append("\",");
                json.append("\"difficulty\":\"").append(JsonUtil.escapeJson(a.getDifficulty())).append("\",");
                json.append("\"articleLikeCount\":").append(a.getArticleLikeCount()).append(",");
                json.append("\"explanationLikeCount\":").append(a.getExplanationLikeCount()).append(",");
                json.append("\"explanationDislikeCount\":").append(a.getExplanationDislikeCount()).append(",");
                json.append("\"vocquizNum\":").append(a.getVocquizNum()).append(",");
                json.append("\"comquizNum\":").append(a.getComquizNum());
                json.append("}");
            }
        } catch (Exception e) {
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"查询文章列表失败: "
                    + JsonUtil.escapeJson(e.getMessage()) + "\"}");
            return;
        }

        json.append("]}");
        response.getWriter().write(json.toString());
    }
}