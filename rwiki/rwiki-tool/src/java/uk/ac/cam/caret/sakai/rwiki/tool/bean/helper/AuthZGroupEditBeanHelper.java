package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupEditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

//FIXME: Tool

public class AuthZGroupEditBeanHelper {

    public static final String REALM_EDIT_BEAN_ATTR = "realmEditBean";
    
    public static AuthZGroupEditBean createRealmEditBean(HttpServletRequest request, ViewBean vb) {
        HttpSession session = request.getSession();
        
        AuthZGroupEditBean rb = null;
        try { 
        		rb = (AuthZGroupEditBean) session.getAttribute(REALM_EDIT_BEAN_ATTR);
        } catch ( ClassCastException ex ) {
        }
        
        if (rb == null) {
            rb = new AuthZGroupEditBean(vb.getPageName(), vb.getLocalSpace());
            session.setAttribute(REALM_EDIT_BEAN_ATTR, rb);
        }
        
        return rb;
    }
    
}
