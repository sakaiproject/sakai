/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.kernel.session.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PreferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

/**
 * @author andrew
 *
 */
public class UpdatePreferencesCommand implements HttpCommand {
    
    private PreferenceService preferenceService;
    private String successfulPath;
    

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RequestScopeSuperBean rssb = RequestScopeSuperBean.getFromRequest(request);
        
        ViewBean vb = rssb.getViewBean();
        EditBean eb = rssb.getEditBean();
        
        String user = rssb.getCurrentUser();
        String localSpace = vb.getLocalSpace();
        
        String notificationLevel = request.getParameter(PreferencesBean.NOTIFICATION_PREFERENCE_PARAM);
        
        if (!PreferenceService.SEPARATE_PREFERENCE.equals(notificationLevel) && !PreferenceService.DIGEST_PREFERENCE.equals(notificationLevel) && !PreferenceService.NONE_PREFERENCE.equals(notificationLevel)) {
            notificationLevel = PreferencesBean.NO_PREFERENCE;
        }
        
        if (EditBean.SAVE_VALUE.equals(eb.getSaveType())) {
            if (PreferencesBean.NO_PREFERENCE.equals(notificationLevel) ) {
                preferenceService.deletePreference(user, localSpace, PreferenceService.MAIL_NOTIFCIATION);
            } else {
                preferenceService.updatePreference(user, localSpace, PreferenceService.MAIL_NOTIFCIATION, notificationLevel);
            }
          
        } 
        this.successfulUpdateDispatch(request, response);
        
        String requestURL = request.getRequestURL().toString();
        SessionManager.getCurrentToolSession().setAttribute(RWikiServlet.SAVED_REQUEST_URL, requestURL + vb.getInfoUrl());
    }

    private void successfulUpdateDispatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(successfulPath);
        rd.forward(request, response);
    }

    public PreferenceService getPreferenceService() {
        return preferenceService;
    }

    public void setPreferenceService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    public String getSuccessfulPath() {
        return successfulPath;
    }

    public void setSuccessfulPath(String successfulPath) {
        this.successfulPath = successfulPath;
    }
        
}
