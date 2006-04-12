package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;

import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

//FIXME: Tool

public class AuthZGroupEditBean extends ViewBean {
    /**
     * Parameter name for the save parameter that indicates what kind of save we
     * should be doing.
     */
    public static final String SAVE_PARAM = "save";

    /**
     * Value of the save parameter that indicates we wish to save the content
     */
    public static final String SAVE_VALUE = "save";

    /**
     * Value of the save parameter that indicates we wish cancel this edit
     */
    public static final String CANCEL_VALUE = "cancel";

    private AuthzGroup realmEdit;

    private List roleBeans;
    
    public AuthZGroupEditBean() { }
    
    public AuthZGroupEditBean(String pageName, String localSpace) {
        super(pageName, localSpace);
    }

    public String getRealmEditUrl() {
        return getPageUrl(getPageName(), WikiPageAction.EDIT_REALM_ACTION.getName());
    }
    
    public AuthzGroup getRealmEdit() {
        return realmEdit;
    }

    public void setRealmEdit(AuthzGroup realmEdit) {
        this.realmEdit = realmEdit;

        this.roleBeans = null;
    }

    public List getRoles() {
        if (roleBeans == null) {
            Set roleset = realmEdit.getRoles();

            if (roleset == null) {
                return new ArrayList();
            }

            Role[] roles = (Role[]) roleset.toArray(new Role[roleset.size()]);

            Arrays.sort(roles);
            roleBeans = new ArrayList(roles.length);
            for (int i = 0; i < roles.length; i++) {
                roleBeans.add(new RoleBean(roles[i]));
            }
        }
        return roleBeans;
    }
}
