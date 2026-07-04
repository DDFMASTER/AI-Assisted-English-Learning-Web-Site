package Filter;

import Listener.AppContextListener;
import Utils.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * 登录拦截过滤器 —— 拦截未登录用户访问受保护的 API 资源。
 *
 * 公开路径（无需登录）：
 *   /api/user/login, /api/user/register, /api/user/logout, /api/connect-test,
 *   /api/word/*, /api/article/list, /api/article/detail,
 *   /api/article/translate-paragraph, /api/article/quiz, /api/article/cultural-notes,
 *   /api/assessment/*, /api/vocabtest/*, /api/clickbait/*
 *
 * 管理路径 /api/admin/* 需要登录且 role == "admin"。
 * 用户写操作 /api/user/* 需要登录。
 *
 * 其余 /api/* 路径均需登录后才能访问。
 * 静态资源（非 /api/ 前缀）直接放行。
 */
@WebFilter("/*")
public class LoginFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    /** 无需登录即可访问的 API 路径（精确匹配） */
    private static final Set<String> PUBLIC_API_PATHS = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/logout",
            "/api/user/profile",
            "/api/connect-test"
    );

    /** 无需登录即可访问的 API 路径前缀 */
    private static final String[] PUBLIC_API_PREFIXES = {
            "/api/word/",
            "/api/article/list",
            "/api/article/detail",
            "/api/article/translate-paragraph",
            "/api/article/quiz",
            "/api/article/cultural-notes",
            "/api/assessment/",
            "/api/vocabtest/",
            "/api/clickbait/"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("LoginFilter 初始化完成");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = path.substring(contextPath.length());

        // 1. 优先检查是否被管理员强制下线
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (isKicked(request.getServletContext(), userId)) {
                session.removeAttribute("userId");
                session.removeAttribute("username");
                session.removeAttribute("role");
                session.invalidate();
                clearKickedFlag(request.getServletContext(), userId);
                sendUnauthorized(response, "您已被管理员强制下线，请重新登录");
                return;
            }
        }

        // 2. 公开路径直接放行
        if (isPublicPath(relativePath)) {
            chain.doFilter(req, res);
            return;
        }

        // 3. 管理路径：需要登录 + admin 角色
        if (relativePath.startsWith("/api/admin/")) {
            if (session == null || session.getAttribute("userId") == null) {
                sendUnauthorized(response, "请先登录");
                return;
            }
            String role = (String) session.getAttribute("role");
            if (!"admin".equals(role)) {
                sendForbidden(response, "无管理员权限");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        // 4. API 路径需要校验登录状态
        if (relativePath.startsWith("/api/")) {
            if (session == null || session.getAttribute("userId") == null) {
                logger.info("拒绝未登录请求: path={}, hasSession={}, userId={}",
                        relativePath, session != null,
                        session != null ? session.getAttribute("userId") : "N/A");
                sendUnauthorized(response, "请先登录");
                return;
            }
        }

        // 5. 放行
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        logger.info("LoginFilter 销毁");
    }

    private boolean isPublicPath(String path) {
        if (PUBLIC_API_PATHS.contains(path)) {
            return true;
        }
        for (String prefix : PUBLIC_API_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return !path.startsWith("/api/");
    }

    private boolean isKicked(ServletContext ctx, Long userId) {
        if (userId == null) return false;
        @SuppressWarnings("unchecked")
        Set<Long> kickedSet = (Set<Long>) ctx.getAttribute(AppContextListener.KICKED_USER_IDS);
        return kickedSet != null && kickedSet.contains(userId);
    }

    private void clearKickedFlag(ServletContext ctx, Long userId) {
        if (userId == null) return;
        @SuppressWarnings("unchecked")
        Set<Long> kickedSet = (Set<Long>) ctx.getAttribute(AppContextListener.KICKED_USER_IDS);
        if (kickedSet != null) {
            kickedSet.remove(userId);
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                "{\"success\":false,\"message\":" + JsonUtil.strVal(message) + ",\"code\":401}");
    }

    private void sendForbidden(HttpServletResponse response, String message)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(
                "{\"success\":false,\"message\":" + JsonUtil.strVal(message) + ",\"code\":403}");
    }
}
