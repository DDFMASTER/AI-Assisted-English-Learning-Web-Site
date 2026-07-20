package Servlet;

import DAO.ArticleDAO;
import DAO.ArticleDAOImpl;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 文章点赞 / 取消点赞
 *
 * POST /api/article/like
 * Body (form-urlencoded): articleId=<id>&action=like|unlike
 *
 * 点赞状态由前端 localStorage 维护，后端仅负责更新计数。
 * 使用 GREATEST(count-1, 0) 防止负数。
 */
@WebServlet("/api/article/like")
public class ArticleLikeServlet extends HttpServlet {

    private final ArticleDAO articleDAO = new ArticleDAOImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 需要登录
        Long userId = ServletUtil.getSessionUserId(request);
        if (userId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        String articleIdStr = request.getParameter("articleId");
        String action = request.getParameter("action");

        if (articleIdStr == null || articleIdStr.isBlank()) {
            response.getWriter().write(JsonUtil.error("缺少 articleId"));
            return;
        }

        long articleId;
        try {
            articleId = Long.parseLong(articleIdStr.trim());
        } catch (NumberFormatException e) {
            response.getWriter().write(JsonUtil.error("articleId 格式错误"));
            return;
        }

        try {
            int rows;
            if ("unlike".equals(action)) {
                rows = articleDAO.decrementLikeCount(articleId);
            } else {
                // 默认 like
                rows = articleDAO.incrementLikeCount(articleId);
            }

            if (rows > 0) {
                response.getWriter().write(
                    "{\"success\":true,\"action\":\"" + (action != null ? action : "like") + "\"}");
            } else {
                response.getWriter().write(JsonUtil.error("文章不存在"));
            }
        } catch (Exception e) {
            response.getWriter().write(JsonUtil.error("操作失败: " + e.getMessage()));
        }
    }
}
