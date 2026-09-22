package uk.ac.cam.caret.sakai;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * In-house Jakarta Servlet replacement for the org.sakaiproject:sakai-jsp-adapter:0.10-K1
 * artifact's uk.ac.cam.caret.sakai.WebappToolServlet, which extends javax.servlet.http.HttpServlet
 * and has no Jakarta-migrated release available. Behavior is a faithful reproduction of the
 * original class (verified via bytecode disassembly of the 0.10-K1 jar).
 */
public class WebappToolServlet extends HttpServlet {

    public static final String FIRST_PAGE = "main-page";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String contextPath = request.getContextPath();

        request.setAttribute("sakai.request.native.url", "sakai.request.native.url");

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getContextPath() {
                return contextPath;
            }
        };

        String mainPage = getInitParameter(FIRST_PAGE);
        if (request.getPathInfo() == null && mainPage != null && !mainPage.equals("/")) {
            response.sendRedirect(contextPath + mainPage);
            return;
        }

        if (request.getPathInfo() == null && !request.getRequestURI().endsWith("/")) {
            response.sendRedirect(contextPath + "/");
            return;
        }

        if (request.getPathInfo() != null
                && (request.getPathInfo().startsWith("/WEB-INF/") || request.getPathInfo().equals("/WEB-INF"))) {
            response.sendRedirect(contextPath + "/");
            return;
        }

        RequestDispatcher dispatcher = request.getPathInfo() == null
                ? request.getRequestDispatcher("/")
                : request.getRequestDispatcher(request.getPathInfo());

        dispatcher.forward(wrappedRequest, response);
    }
}
