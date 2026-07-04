package Servlet;

import Service.UserService;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 每日签到
 * POST /api/user/checkin
 * 从 session 获取 userId，返回获得的经验值。
 */
@WebServlet("/api/user/checkin")
public class UserCheckinServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = ServletUtil.getSessionUserId(request);
        if (userId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        int result = userService.checkin(userId);
        switch (result) {
            case -1 -> response.getWriter().write(
                    JsonUtil.error("今日已签到，明天再来吧"));
            case -2 -> response.getWriter().write(
                    JsonUtil.error("用户不存在"));
            default -> {
                String extra = "\"experienceEarned\":" + JsonUtil.numVal(result);
                response.getWriter().write(
                        JsonUtil.buildResponse(true, "签到成功！经验值 +" + result, extra));
            }
        }
    }
}
