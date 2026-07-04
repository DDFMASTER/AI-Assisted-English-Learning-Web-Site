package Servlet;

import Entities.User;
import Service.UserService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@WebServlet("/api/user/login")
public class UserLoginServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userService.login(username, password);

        if (user == null) {
            response.getWriter().write(
                    JsonUtil.error("用户名或密码错误"));
            return;
        }

        // 检查 VIP 是否过期
        userService.checkVipExpired(user);

        // 创建/获取会话，存储用户登录状态
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setAttribute("studyPurpose", user.getStudyPurpose());

        // 设置会话超时（从配置文件读取，默认 30 分钟）
        int timeoutMinutes = Utils.ConfigUtil.getInt("session.timeout.minutes", 30);
        session.setMaxInactiveInterval(timeoutMinutes * 60);

        System.out.println("[AAEL] 用户登录成功: userId=" + user.getUserId()
                + ", username=" + user.getUsername()
                + ", role=" + user.getRole()
                + ", sessionId=" + session.getId());

        // VIP 到期时间
        String vipExpireAt = "";
        if ("vip".equals(user.getProfile()) && user.getVipUntil() != null) {
            vipExpireAt = user.getVipUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        // 构建成功响应
        String extra = "\"userId\":" + JsonUtil.numVal(user.getUserId())
                + ",\"username\":" + JsonUtil.strVal(user.getUsername())
                + ",\"role\":" + JsonUtil.strVal(user.getRole())
                + ",\"profile\":" + JsonUtil.strVal(user.getProfile())
                + ",\"studyPurpose\":" + JsonUtil.strVal(user.getStudyPurpose())
                + ",\"experience\":" + JsonUtil.numVal(user.getExperience())
                + ",\"cefrProgress\":" + JsonUtil.numVal(user.getCefrProgress())
                + ",\"literacy\":" + JsonUtil.numVal(user.getLiteracy())
                + ",\"vipExpireAt\":" + JsonUtil.strVal(vipExpireAt);

        response.getWriter().write(JsonUtil.buildResponse(true, "登录成功", extra));
    }
}
