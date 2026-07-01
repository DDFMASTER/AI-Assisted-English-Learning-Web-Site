package Servlet;

import Service.UserService;
import DAO.UserDAOImpl;
import Entities.User;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * VIP 兑换接口
 *
 * POST /api/user/vip-exchange
 * 参数: userId (Long), days (int)
 *
 * 180 经验值 = 1 天 VIP
 * 响应: { success, message, newExperience, vipExpireAt, role }
 */
@WebServlet("/api/user/vip-exchange")
public class VipExchangeServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = parseLong(request.getParameter("userId"));
        int days = parseInt(request.getParameter("days"), 1);

        if (userId == null) {
            response.getWriter().write(JsonUtil.error("缺少 userId"));
            return;
        }

        String err = userService.exchangeVip(userId, days);
        if (err != null) {
            switch (err) {
                case "INSUFFICIENT_XP" ->
                    response.getWriter().write(JsonUtil.error("经验值不足，需要 " + (days * 180) + " 经验值"));
                case "USER_NOT_FOUND" ->
                    response.getWriter().write(JsonUtil.error("用户不存在"));
                default ->
                    response.getWriter().write(JsonUtil.error(err));
            }
            return;
        }

        // 查询更新后的用户信息
        User user = new UserDAOImpl().findById(userId);
        int newXp = user.getExperience() != null ? user.getExperience() : 0;
        LocalDateTime expireAt = user.getLastCheckin();
        String expireStr = expireAt != null ? expireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";

        String extra = "\"newExperience\":" + JsonUtil.numVal(newXp)
                + ",\"vipExpireAt\":" + JsonUtil.strVal(expireStr)
                + ",\"profile\":" + JsonUtil.strVal(user.getProfile());

        response.getWriter().write(JsonUtil.buildResponse(true, "VIP 兑换成功", extra));
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private int parseInt(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
    }
}
