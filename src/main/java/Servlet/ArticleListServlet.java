package Servlet;

import Utils.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章列表（支持分类筛选、难度筛选、分页）
 *
 * GET /api/article/list
 *   可选参数: category, difficulty, page, pageSize, limit
 *
 *   category: basic(初中/高中) | exam(四级/六级) | advanced(考研/托福/期刊/原著) | extended(网络新闻)
 *   difficulty: 精确匹配难度字段
 *   page / pageSize: 分页（默认 page=1, pageSize=10）
 */
@WebServlet("/api/article/list")
public class ArticleListServlet extends HttpServlet {

    private static final java.util.Map<String, String[]> CATEGORY_MAP = java.util.Map.of(
        "basic",    new String[]{"初中", "高中"},
        "exam",     new String[]{"四级", "六级"},
        "advanced", new String[]{"考研", "托福", "期刊", "原著"},
        "extended", new String[]{"网络新闻"}
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String difficulty = request.getParameter("difficulty");
        String category = request.getParameter("category");
        String limitStr = request.getParameter("limit");

        int page = 1;
        int pageSize = 10;
        try { page = Integer.parseInt(request.getParameter("page")); } catch (Exception ignored) {}
        try { pageSize = Integer.parseInt(request.getParameter("pageSize")); } catch (Exception ignored) {}
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 50) pageSize = 50;

        // limit 参数优先级高于分页
        boolean useLimit = limitStr != null && !limitStr.isBlank();
        int limit = 100;
        if (useLimit) {
            try { limit = Integer.parseInt(limitStr); } catch (Exception ignored) {}
            if (limit < 1) limit = 1;
            if (limit > 50) limit = 50;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 构建 WHERE 子句
            List<String> conditions = new ArrayList<>();
            List<String> params = new ArrayList<>();

            if (difficulty != null && !difficulty.isBlank()) {
                // 支持逗号分隔的多难度（C2 匹配多个）
                String[] diffs = difficulty.split(",");
                List<String> placeholders = new ArrayList<>();
                for (String d : diffs) {
                    placeholders.add("?");
                    params.add(d.trim());
                }
                conditions.add("difficulty IN (" + String.join(",", placeholders) + ")");
            }

            if (category != null && !category.isBlank()) {
                String[] diffs = CATEGORY_MAP.get(category);
                if (diffs != null) {
                    List<String> placeholders = new ArrayList<>();
                    for (String d : diffs) {
                        placeholders.add("?");
                        params.add(d);
                    }
                    conditions.add("difficulty IN (" + String.join(",", placeholders) + ")");
                }
            }

            String whereClause = "";
            if (!conditions.isEmpty()) {
                whereClause = " WHERE " + String.join(" AND ", conditions);
            }

            // 查询总数
            int total = 0;
            String countSql = "SELECT COUNT(*) FROM article" + whereClause;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            // 查询文章
            String sql = "SELECT article_id, title, source, difficulty, " +
                         "article_like_count, explanation_like_count, " +
                         "explanation_dislike_count, vocquiz_num, comquiz_num, " +
                         "CHAR_LENGTH(content) AS content_length " +
                         "FROM article" + whereClause +
                         " ORDER BY article_id DESC";

            if (useLimit) {
                sql += " LIMIT " + limit;
            } else {
                int offset = (page - 1) * pageSize;
                sql += " LIMIT " + pageSize + " OFFSET " + offset;
            }

            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true");
            if (!useLimit) {
                json.append(",\"total\":").append(total);
                json.append(",\"page\":").append(page);
                json.append(",\"pageSize\":").append(pageSize);
                int totalPages = (int) Math.ceil((double) total / pageSize);
                json.append(",\"totalPages\":").append(totalPages);
            }
            json.append(",\"articles\":[");

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;

                        int contentLen = rs.getInt("content_length");
                        int wordCount = Math.max(1, contentLen / 5);
                        String articleDifficulty = rs.getString("difficulty");
                        String studyPurpose = Utils.ReadingTimeUtil.getStudyPurpose(request);
                        double readTime = Utils.ReadingTimeUtil.calculate(contentLen, articleDifficulty, studyPurpose);

                        json.append("{");
                        json.append("\"articleId\":").append(rs.getLong("article_id")).append(",");
                        json.append("\"title\":\"").append(esc(rs.getString("title"))).append("\",");
                        json.append("\"source\":\"").append(esc(rs.getString("source"))).append("\",");
                        json.append("\"difficulty\":\"").append(esc(articleDifficulty)).append("\",");
                        json.append("\"wordCount\":").append(wordCount).append(",");
                        json.append("\"readTime\":").append(readTime).append(",");
                        json.append("\"articleLikeCount\":").append(rs.getInt("article_like_count")).append(",");
                        json.append("\"explanationLikeCount\":").append(rs.getInt("explanation_like_count")).append(",");
                        json.append("\"explanationDislikeCount\":").append(rs.getInt("explanation_dislike_count")).append(",");
                        json.append("\"vocquizNum\":").append(rs.getInt("vocquiz_num")).append(",");
                        json.append("\"comquizNum\":").append(rs.getInt("comquiz_num"));
                        json.append("}");
                    }
                }
            }

            json.append("]}");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"查询文章列表失败: " + esc(e.getMessage()) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
