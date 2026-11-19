package org.webjars.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * In-house Jakarta Servlet replacement for org.webjars:webjars-servlet-2.x, which has never had a
 * Jakarta-migrated release (verified: even the latest 1.6 still extends javax.servlet.http.HttpServlet).
 * Behavior is a faithful reproduction of the original class (verified via bytecode disassembly of the
 * 1.6 jar), minus the optional webjars-locator-core version-agnostic resolution path, which this module
 * never depended on in the first place (so it always fell through to the direct classpath lookup below).
 */
public class WebjarsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(WebjarsServlet.class.getName());

    private static final long DEFAULT_EXPIRE_TIME_MS = 86400000L;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private boolean disableCache = false;

    @Override
    public void init() throws ServletException {
        final ServletConfig config = getServletConfig();
        if (config == null) {
            throw new NullPointerException("Expected servlet container to provide a non-null ServletConfig.");
        }

        try {
            final String disableCacheParam = config.getInitParameter("disableCache");
            if (disableCacheParam != null) {
                this.disableCache = Boolean.parseBoolean(disableCacheParam);
                logger.log(Level.INFO, "WebjarsServlet cache enabled: {0}", !this.disableCache);
            }
        } catch (final Exception e) {
            logger.log(Level.WARNING, "The WebjarsServlet configuration parameter \"disableCache\" is invalid");
        }

        logger.log(Level.INFO, "WebjarsServlet initialization completed");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final String path = request.getRequestURI().replaceFirst(request.getContextPath(), "");
        final String webjarPath = "/META-INF/resources" + path;

        logger.log(Level.FINE, "Webjars resource requested: {0}", webjarPath);

        if (isDirectoryRequest(webjarPath)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String etag;
        try {
            etag = getETagName(webjarPath);
        } catch (final IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!this.disableCache && (checkETagMatch(request, etag) || checkLastModify(request))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        try (InputStream resourceStream = getClass().getResourceAsStream(webjarPath)) {
            if (resourceStream == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (!this.disableCache) {
                prepareCacheHeaders(response, etag);
            }

            final String fileName = getFileName(webjarPath);
            final ServletContext context = getServletContext();
            final String mimeType = context.getMimeType(fileName);
            response.setContentType(mimeType != null ? mimeType : "application/octet-stream");

            try (OutputStream out = response.getOutputStream()) {
                copy(resourceStream, out);
            }
        }
    }

    private static boolean isDirectoryRequest(final String path) {
        return path.endsWith("/");
    }

    private String getFileName(final String path) {
        final String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private String getETagName(final String path) {
        final String[] parts = path.split("/");
        if (parts.length < 7) {
            throw new IllegalArgumentException("insufficient URL has given:" + path);
        }
        final String version = parts[5];
        final String fileName = parts[parts.length - 1];
        return fileName + "_" + version;
    }

    private boolean checkETagMatch(final HttpServletRequest request, final String etag) {
        final String ifNoneMatch = request.getHeader("If-None-Match");
        return ifNoneMatch != null && ifNoneMatch.equals(etag);
    }

    private boolean checkLastModify(final HttpServletRequest request) {
        final long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        return ifModifiedSince != -1 && (ifModifiedSince - System.currentTimeMillis()) > 0;
    }

    private void prepareCacheHeaders(final HttpServletResponse response, final String etag) {
        response.setHeader("ETag", etag);
        response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS);
        response.addDateHeader("Last-Modified", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS);
        response.addHeader("Cache-Control", "private, max-age=86400");
    }

    private static int copy(final InputStream input, final OutputStream output) throws IOException {
        long count = 0;
        int n;
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count > Integer.MAX_VALUE ? -1 : (int) count;
    }
}
