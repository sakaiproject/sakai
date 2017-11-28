package org.sakaiproject.rest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.PasswordCheck;
import org.sakaiproject.util.Validator;
import org.sakaiproject.webservices.interceptor.NoIPRestriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.sakaiproject.rest.AddUser.AddStatus.Message.*;

/**
 * These are a set of endpoint designed to support the adding of a new user to a site.
 * They are not deigned to be generic and aren't deisgned to be used independently of the
 * JavaScript frontend.
 */
@Path("/add-user")
public class AddUser {

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    @Qualifier("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage")
    private UserAuditRegistration userAuditRegistration;

    @Autowired
    private UserNotificationProvider userNotificationProvider;


    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setUserAuditRegistration(UserAuditRegistration userAuditRegistration) {
        this.userAuditRegistration = userAuditRegistration;
    }

    public void setUserNotificationProvider(UserNotificationProvider userNotificationProvider) {
        this.userNotificationProvider = userNotificationProvider;
    }

    @PostConstruct
    public void init() {
        Objects.requireNonNull(userDirectoryService);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(serverConfigurationService);
        Objects.requireNonNull(userAuditRegistration);
        Objects.requireNonNull(userNotificationProvider);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @NoIPRestriction
    public List<UserResult> search(@NotNull @QueryParam("q") String query) {

        checkLoggedIn();
        List<User> externalUsers = userDirectoryService.searchExternalUsers(query, 1, 10);
        List<User> internalUsers = userDirectoryService.searchUsers(query, 1, 10);

        List<User> users = new ArrayList<>();
        users.addAll(externalUsers);
        users.addAll(internalUsers);

        users.sort(Comparator.comparing(User::getDisplayName));
        users = users.subList(0, Math.min(users.size(), 10));

        List<UserResult> userResults = users.stream().map(UserResult::new).collect(Collectors.toList());
        return userResults;
    }

    // This is needed so we don't get the provider only roles and also get the descriptions.
    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @NoIPRestriction
    public List<RoleDetails> roles(@NotNull @QueryParam("siteId") String siteId) {
        try {
            List<RoleDetails> roleDetails = new ArrayList<>();
            Site site = siteService.getSiteVisit(siteId);
            for (Role role : site.getRoles()) {
                if (!role.isProviderOnly()) {
                    roleDetails.add(new RoleDetails(role.getId(), role.getDescription()));
                }
            }
            return roleDetails;
        } catch (IdUnusedException e) {
            throw new NotFoundException("Failed to find site of: " + siteId);
        } catch (PermissionException e) {
            throw new ForbiddenException("No permission to get site: " + siteId);
        }
    }

    public static class RoleDetails {
        private String id;
        private String description;

        public RoleDetails(String role, String description) {
            this.id = role;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    @GET
    @Path("/lookup")
    @NoIPRestriction
    @Produces(MediaType.APPLICATION_JSON)
    public UserLookup lookup(@NotNull @QueryParam("search") String search) {
        checkLoggedIn();
        User user;
        try {
            user = userDirectoryService.getUser(search);
        } catch (UserNotDefinedException e) {
            try {
                user = userDirectoryService.getUserByAid(search);
            } catch (UserNotDefinedException e1) {
                Collection<User> users = userDirectoryService.findUsersByEmail(search);
                if (users.size() == 1) {
                    user = users.iterator().next();
                } else {
                    // Check if email?
                    boolean allowAdd = userDirectoryService.allowAddUser();
                    return new UserLookup(allowAdd);
                }
            }
        }
        return new UserLookup(user);
    }

    public static class UserLookup {
        private boolean allowAdd;
        private UserResult user;

        UserLookup(User user) {
            this.user = new UserResult(user);
            this.allowAdd = false;
        }

        UserLookup(boolean allowAdd) {
            this.user = null;
            this.allowAdd = allowAdd;
        }

        public boolean isAllowAdd() {
            return allowAdd;
        }

        public UserResult getUser() {
            return user;
        }
    }

    public static class UserResult {
        private String id;
        private String displayId;
        private String email;
        private String displayName;

        UserResult(User u) {
            this.id = u.getId();
            this.displayId = u.getDisplayId();
            this.email = u.getEmail();
            this.displayName = u.getDisplayName();
        }

        public String getId() {
            return id;
        }

        public String getDisplayId() {
            return displayId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    @GET
    @Path("/site")
    @NoIPRestriction
    @Produces(MediaType.APPLICATION_JSON)
    public SiteStatus status(@NotNull @QueryParam("siteId") String siteId, @NotNull @QueryParam("userId") String userId) {
        try {
            Site site = siteService.getSiteVisit(siteId);
            Role userRole = site.getUserRole(userId);
            SiteStatus status;
            if (userRole != null) {
                status = new SiteStatus(siteId, userId, userRole);
            } else {
                status = new SiteStatus(siteId, userId);
            }
            return status;

        } catch (IdUnusedException e) {
            throw new NotFoundException("Failed to find site of: " + siteId);
        } catch (PermissionException e) {
            throw new ForbiddenException("Not allowed to access site: " + siteId);
        }
    }

    public static class SiteStatus {
        private final String siteId;
        private final String userId;
        private final String roleId;

        public SiteStatus(String siteId, String userId) {
            this(siteId, userId, (String) null);
        }

        public SiteStatus(String siteId, String userId, Role userRole) {
            this(siteId, userId, userRole.getId());
        }

        public SiteStatus(String siteId, String userId, String roleId) {
            this.siteId = siteId;
            this.userId = userId;
            this.roleId = roleId;
        }

        public String getSiteId() {
            return siteId;
        }

        public String getUserId() {
            return userId;
        }

        public String getRoleId() {
            return roleId;
        }
    }

    @POST
    @Path("/adds")
    @NoIPRestriction
    @Produces(MediaType.APPLICATION_JSON)
    public List<AddStatus> adds(@NotNull @FormParam("siteId") String siteId,
                                @NotNull @FormParam("roleId") String roleId,
                                @NotNull @FormParam("userIds") List<String> userIds,
                                @FormParam("notify") boolean notify) {
        try {
            String currentUserId = userDirectoryService.getCurrentUser().getEid();
            Site site = siteService.getSiteVisit(siteId);
            List<AddStatus> statuses = new ArrayList<>();
            List<String[]> audit = new ArrayList<>();
            boolean needToSave = false;
            for (String userId : userIds) {
                AddStatus status = new AddStatus(siteId, userId, roleId);
                statuses.add(status);
                if (!status.getUserId().isEmpty()) {
                    boolean added = addUserToSite(status, site);
                    needToSave |= added;
                    if (added) {
                        String[] userAuditString = {siteId, status.getUser().getEid(), roleId,
                            UserAuditService.USER_AUDIT_ACTION_ADD, userAuditRegistration.getDatabaseSourceKey(),
                            currentUserId
                        };
                        audit.add(userAuditString);
                    }
                }
            }
            if (needToSave) {
                siteService.saveSiteMembership(site);
            }
            if (!audit.isEmpty()) {
                userAuditRegistration.addToUserAuditing(audit);
            }
            if (notify) {
                for (AddStatus status : statuses) {
                    if (status.added) {
                        User user = status.getUser();
                        userNotificationProvider.notifyAddedParticipant(!isOfficialAccount(user.getEid()), user, site);
                    }
                }
            }
            return statuses;
        } catch (IdUnusedException e) {
            throw new NotFoundException("Failed to find site: " + siteId);
        } catch (PermissionException e) {
            throw new ForbiddenException("Not allowed to access site: " + siteId);
        }
    }

    private boolean isOfficialAccount(String eId) {
        return !eId.contains("@");
    }

    private boolean addUserToSite(AddStatus status, Site site) {
        User user = resolveUser(status, site);
        if (user != null) {
            boolean externalUsers = allowExternalUsers(site);
            if (!externalUsers && "guest".equals(user.getType())) {
                status.setMessage(user_external_not_allowed);
            } else {
                if (site.getUserRole(user.getId()) == null) {
                    site.addMember(user.getId(), status.getRoleId(), true, false);
                    status.setAdded(true);
                    status.setUser(user);
                    return true;
                } else {
                    status.setMessage(user_already_member);
                }
            }
        }
        return false;
    }

    private boolean allowExternalUsers(Site site) {
        boolean externalUsers = serverConfigurationService.getBoolean("nonOfficialAccount", true);
        if (!externalUsers) {
            String nonOfficialAccountSite = site.getProperties().getProperty("nonOfficialAccount");
            if (nonOfficialAccountSite != null) {
                externalUsers = Boolean.valueOf(nonOfficialAccountSite);
            }
        }
        return externalUsers;
    }

    private User resolveUser(AddStatus status, Site site) {
        String userId = status.getUserId();
        User user = null;
        try {
            user = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            if (EmailValidator.getInstance().isValid(userId)) {
                if (isValidDomain(userId)) {
                    try {
                        UserEdit userEdit = userDirectoryService.addUser(null, userId);
                        userEdit.setEmail(userId);
                        String pw = PasswordCheck.generatePassword();
                        userEdit.setPassword(pw);
                        userEdit.setType("guest");
                        userDirectoryService.commitEdit(userEdit);
                        user = userEdit;
                        userNotificationProvider.notifyNewUserEmail(user, pw, site);
                    } catch (UserIdInvalidException e1) {
                        status.setMessage(user_invalid_id);
                    } catch (UserAlreadyDefinedException e1) {
                        status.setMessage(user_exists);
                    } catch (UserPermissionException e1) {
                        status.setMessage(no_permission_create);
                    }
                } else {
                    status.setMessage(user_email_bad_domain);
                }
            } else {
                status.setMessage(user_not_found);
            }
        }
        // If we failed to resolve the user then
        if (user == null && status.getMessage() == null) {
            status.setMessage(unknown_error);
        }
        return user;
    }

    protected boolean isValidDomain(String address) {
        List<String> invalidDomains = Arrays.asList( ArrayUtils.nullToEmpty( serverConfigurationService.getStrings( "invalidEmailInIdAccountString" ) ) );
        return !invalidDomains.stream().map(String::toLowerCase).anyMatch(domain -> address.toLowerCase().endsWith(domain));

    }

    protected void checkLoggedIn() throws NotAllowedException {
       if (userDirectoryService.getCurrentUser().equals(userDirectoryService.getAnonymousUser())) {
           throw new NotAllowedException("Cannot access as anonymous.");
       }

    }

    public static class AddStatus extends SiteStatus {

        /**
         * Possible messages to make it easier to make sure all i18n are present.
          */
        enum Message {
            user_invalid_id, user_exists, no_permission_create, user_email_bad_domain, user_not_found,
            user_external_not_allowed, user_already_member, unknown_error;
        }

        private boolean added;
        private Message message;
        private User user;

        public AddStatus(String siteId, String userId, String role) {
            super(siteId, userId, role);
        }

        public boolean isAdded() {
            return added;
        }

        public Message getMessage() {
            return message;
        }

        public void setAdded(boolean added) {
            this.added = added;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        protected User getUser() {
            return user;
        }

        protected void setUser(User user) {
            this.user = user;
        }
    }
}
