package Filter;

import Utils.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 全局异常处理 Filter —— 将未捕获异常转换为 JSON 错误响应。
 * 不暴露完整堆栈追踪，仅返回摘要信息。
 */
@WebFilter("/*")
public class ExceptionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            String path = request.getRequestURI();
            logger.error("未处理的异常: path={}, type={}, message={}", path,
                    e.getClass().getSimpleName(), e.getMessage(), e);

            // 仅对 API 请求返回 JSON 错误
            if (path.contains("/api/")) {
                if (!response.isCommitted()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    String detail = e.getClass().getSimpleName();
                    if (e.getMessage() != null) {
                        detail += ": " + e.getMessage();
                    }
                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"服务器内部错误\",\"code\":500,\"detail\":"
                                    + JsonUtil.strVal(detail) + "}");
                }
            } else {
                // 非 API 请求，让容器默认处理
                throw e;
            }
        }
    }
}
