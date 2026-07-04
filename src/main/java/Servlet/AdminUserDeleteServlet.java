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
 * 管理员 — 删除用户
 *
 * POST /api/admin/user/delete
 * Body: adminUserId, userId
 */
@WebServlet("/api/admin/user/delete")
public class AdminUserDeleteServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long adminUserId = ServletUtil.authenticateAdmin(request, response, adminService);
        if (adminUserId == null) return;

        Long targetUserId = ServletUtil.parseLong(request.getParameter("userId"));

        String err = adminService.deleteUser(targetUserId, adminUserId);
        if (err != null) {
            response.getWriter().write(JsonUtil.error(err));
            return;
        }

        // 记录日志
        adminService.logAction(adminUserId, "user", targetUserId,
                "delete", null);

        response.getWriter().write(JsonUtil.success("用户已删除"));
    }
}
