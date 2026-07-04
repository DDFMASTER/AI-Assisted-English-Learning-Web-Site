package Servlet;

import DAO.ArticleDAO;
import DAO.ArticleDAOImpl;
import Entities.Article;
import Service.AdminService;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 管理员 — 获取单篇文章详情（含 content，用于编辑）
 *
 * GET /api/admin/article/get?id=xxx&adminUserId=xxx
 */
@WebServlet("/api/admin/article/get")
public class AdminArticleGetServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final ArticleDAO articleDAO = new ArticleDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long adminUserId = ServletUtil.authenticateAdmin(request, response, adminService);
            if (adminUserId == null) return;

            Long articleId = ServletUtil.parseLong(request.getParameter("id"));
            if (articleId == null) {
                response.getWriter().write(JsonUtil.error("缺少文章ID"));
                return;
            }

            Article article = articleDAO.findById(articleId);
            if (article == null) {
                response.getWriter().write(JsonUtil.error("文章不存在"));
                return;
            }

            // 返回文章完整信息（使用 Gson 序列化）
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("success", true);
            var am = new java.util.LinkedHashMap<String, Object>();
            am.put("articleId", article.getArticleId());
            am.put("title", article.getTitle());
            am.put("content", article.getContent());
            am.put("source", article.getSource());
            am.put("difficulty", article.getDifficulty());
            map.put("article", am);
            response.getWriter().write(Utils.GsonUtil.toJson(map));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write(JsonUtil.error("服务器错误: " + e.getMessage()));
        }
    }
}
