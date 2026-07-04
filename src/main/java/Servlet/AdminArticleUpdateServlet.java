package Servlet;

import Service.AdminService;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 管理员 — 更新文章
 *
 * POST /api/admin/article/update
 * Body: adminUserId, articleId, title, content, source, difficulty
 */
@WebServlet("/api/admin/article/update")
public class AdminArticleUpdateServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long adminUserId = Utils.ServletUtil.authenticateAdmin(request, response, adminService);
            if (adminUserId == null) return;

            Long articleId = ServletUtil.parseLong(request.getParameter("articleId"));
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String source = request.getParameter("source");
            String difficulty = request.getParameter("difficulty");

            String err = adminService.updateArticle(articleId, title, content, source, difficulty);
            if (err != null) {
                response.getWriter().write(JsonUtil.error(err));
                return;
            }

            adminService.logAction(adminUserId, "article", articleId,
                    "update", "title=" + title);

            response.getWriter().write(JsonUtil.success("文章更新成功"));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write(JsonUtil.error("服务器错误: " + e.getMessage()));
        }
    }
}
