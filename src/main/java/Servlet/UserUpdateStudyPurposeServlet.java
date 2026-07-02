package Servlet;

import DAO.UserDAOImpl;
import Utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 更新用户学习阶段
 * POST /api/user/update-study-purpose
 * 参数: userId (Long), studyPurpose (String)
 */
@WebServlet("/api/user/update-study-purpose")
public class UserUpdateStudyPurposeServlet extends HttpServlet {

    private static final String[] VALID_STAGES = {
        "初中", "高中", "四级", "六级", "考研", "托福", "期刊", "原著", "网络新闻"
    };

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = parseLong(request.getParameter("userId"));
        String studyPurpose = request.getParameter("studyPurpose");

        if (userId == null) {
            response.getWriter().write(JsonUtil.error("缺少 userId"));
            return;
        }
        if (studyPurpose == null || studyPurpose.isBlank()) {
            response.getWriter().write(JsonUtil.error("缺少 studyPurpose"));
            return;
        }

        // 校验合法性
        boolean valid = false;
        for (String s : VALID_STAGES) {
            if (s.equals(studyPurpose.trim())) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            response.getWriter().write(JsonUtil.error("无效的学习阶段"));
            return;
        }

        var dao = new UserDAOImpl();
        var user = dao.findById(userId);
        if (user == null) {
            response.getWriter().write(JsonUtil.error("用户不存在"));
            return;
        }

        dao.updateStudyPurpose(userId, studyPurpose.trim());
        response.getWriter().write(JsonUtil.success("学习阶段已更新为 " + studyPurpose.trim()));
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
