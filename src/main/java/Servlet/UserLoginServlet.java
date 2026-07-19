package Servlet;

import Entities.User;
import Service.LoginAttemptService;
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
    private final LoginAttemptService loginAttemptService = new LoginAttemptService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 1. 检查账户是否被锁定
        if (loginAttemptService.isLocked(username)) {
            long lockSeconds = loginAttemptService.getLockSecondsRemaining(username);
            response.getWriter().write(
                    JsonUtil.buildResponse(false,
                            "账户已锁定，请于 " + formatLockTime(lockSeconds) + " 后重试",
                            "\"lockSeconds\":" + lockSeconds));
            return;
        }

        User user = userService.login(username, password);

        if (user == null) {
            // 2. 记录失败（用户名无效或密码错误均记录，防止用户枚举）
            int remaining = loginAttemptService.recordFailure(username);
            String msg;
            String extra = "\"remainingAttempts\":" + remaining;
            if (remaining <= 0) {
                msg = "密码错误次数过多，账户已锁定 15 分钟";
                extra += ",\"lockSeconds\":" + loginAttemptService.getLockSecondsRemaining(username);
            } else {
                msg = "用户名或密码错误（剩余 " + remaining + " 次尝试）";
            }
            response.getWriter().write(JsonUtil.buildResponse(false, msg, extra));
            return;
        }

        // 3. 登录成功，清除失败记录
        loginAttemptService.clearAttempts(username);

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

    /** 将秒数格式化为人类可读的等待时间 */
    private static String formatLockTime(long seconds) {
        if (seconds <= 0) return "片刻";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0 && secs > 0) return minutes + " 分 " + secs + " 秒";
        if (minutes > 0) return minutes + " 分钟";
        return secs + " 秒";
    }
}
