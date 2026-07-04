package Servlet;

import Service.UserService;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/api/user/register")
public class UserRegisterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterServlet.class);
    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String studyPurpose = request.getParameter("studyPurpose");

        logger.info("收到注册请求: username={}, studyPurpose={}", username, studyPurpose);

        String error = userService.register(username, password, studyPurpose);

        if (error != null) {
            logger.info("注册失败: username={}, reason={}", username, error);
            response.getWriter().write(JsonUtil.error(error));
            return;
        }

        logger.info("注册成功: username={}", username);
        response.getWriter().write(JsonUtil.success("注册成功"));
    }
}
