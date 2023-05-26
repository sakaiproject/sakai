package org.sakaiproject.tool.assessment.ui.servlet;

import java.util.Optional;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

public abstract class SamigoBaseServlet extends HttpServlet {


    private SecurityService securityService = ComponentManager.get(SecurityService.class);
    private UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);


    public boolean hasPrivilege(String functionName, String siteId) {
        return securityService.unlock(functionName, "/site/" + siteId);
    }

    public Optional<String> getUserId() {
        User user = userDirectoryService.getCurrentUser();

        return user != null && StringUtils.isNotEmpty(user.getId()) ? Optional.of(user.getId()) : Optional.empty();
    }
}
