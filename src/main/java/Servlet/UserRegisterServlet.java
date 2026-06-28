package Servlet;

import Service.UserService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/user/register")
public class UserRegisterServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String studyPurpose = request.getParameter("studyPurpose");

        String error = userService.register(username, password, studyPurpose);

        if (error != null) {
            response.getWriter().write(JsonUtil.error(error));
            return;
        }

        response.getWriter().write(JsonUtil.success("注册成功"));
    }
}
