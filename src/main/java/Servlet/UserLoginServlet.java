package Servlet;

import Entities.User;
import Service.UserService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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

        // 构建成功响应
        String extra = "\"userId\":" + JsonUtil.numVal(user.getUserId())
                + ",\"username\":" + JsonUtil.strVal(user.getUsername())
                + ",\"role\":" + JsonUtil.strVal(user.getRole())
                + ",\"studyPurpose\":" + JsonUtil.strVal(user.getStudyPurpose())
                + ",\"experience\":" + JsonUtil.numVal(user.getExperience())
                + ",\"literacy\":" + JsonUtil.numVal(user.getLiteracy());

        response.getWriter().write(JsonUtil.buildResponse(true, "登录成功", extra));
    }
}
