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
 * 管理员为用户授予 VIP 天数。
 * POST /api/admin/user/grant-vip
 * 参数: targetUserId, days
 */
@WebServlet("/api/admin/user/grant-vip")
public class AdminUserGrantVipServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long adminUserId = ServletUtil.authenticateAdmin(request, response, adminService);
            if (adminUserId == null) return;

            Long targetUserId = ServletUtil.parseLong(request.getParameter("targetUserId"));
            int days = ServletUtil.parseInt(request.getParameter("days"), 0);

            String err = adminService.grantVip(targetUserId, days);
            if (err != null) {
                response.getWriter().write(JsonUtil.error(err));
                return;
            }

            adminService.logAction(adminUserId, "user", targetUserId,
                    "grant_vip", "days=" + days);

            response.getWriter().write(JsonUtil.success("已为用户授予 " + days + " 天 VIP"));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write(JsonUtil.error("服务器错误: " + e.getMessage()));
        }
    }
}
