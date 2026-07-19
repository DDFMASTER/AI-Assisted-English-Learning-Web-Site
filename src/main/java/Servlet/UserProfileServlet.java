package Servlet;

import Entities.User;
import DAO.UserDAOImpl;
import Service.UserService;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户个人信息接口。
 * GET /api/user/profile
 * 从 Session 中获取当前登录用户，返回用户完整信息（含经验值、学习阶段等）。
 */
@WebServlet("/api/user/profile")
public class UserProfileServlet extends HttpServlet {

    private final UserDAOImpl userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 从 Session 获取当前登录用户 ID（不再接受 query param，防止用户枚举）
        Long sessionUserId = ServletUtil.getSessionUserId(request);
        if (sessionUserId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        User user = userDAO.findById(sessionUserId);
        if (user == null) {
            response.getWriter().write(JsonUtil.error("用户不存在"));
            return;
        }

        // 检查 VIP 是否过期
        UserService userService = new UserService();
        userService.checkVipExpired(user);

        // 重新读取以确保 role 最新
        user = userDAO.findById(user.getUserId());

        // VIP 到期时间
        String vipExpireAt = "";
        if ("vip".equals(user.getProfile()) && user.getVipUntil() != null) {
            vipExpireAt = user.getVipUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        // 构建用户信息 JSON
        String extra = "\"user\":{"
                + "\"userId\":" + JsonUtil.numVal(user.getUserId())
                + ",\"username\":" + JsonUtil.strVal(user.getUsername())
                + ",\"role\":" + JsonUtil.strVal(user.getRole())
                + ",\"profile\":" + JsonUtil.strVal(user.getProfile())
                + ",\"studyPurpose\":" + JsonUtil.strVal(user.getStudyPurpose())
                + ",\"experience\":" + JsonUtil.numVal(user.getExperience())
                + ",\"cefrProgress\":" + JsonUtil.numVal(user.getCefrProgress())
                + ",\"literacy\":" + JsonUtil.numVal(user.getLiteracy())
                + ",\"vipExpireAt\":" + JsonUtil.strVal(vipExpireAt)
                + ",\"lastCheckin\":" + (user.getLastCheckin() != null
                    ? "\"" + user.getLastCheckin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\""
                    : "null")
                + "}";

        response.getWriter().write(
                JsonUtil.buildResponse(true, "查询成功", extra));
    }
}
