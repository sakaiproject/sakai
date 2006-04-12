/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author ieb
 *
 */
public class CommentSaveCommand extends SaveCommand {

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.tool.command.SaveCommand#successfulUpdateDispatch(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void successfulUpdateDispatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestScopeSuperBean rssb = RequestScopeSuperBean
            .getFromRequest(request);

        ViewParamsHelperBean vphb = (ViewParamsHelperBean) rssb
            .getNameHelperBean();

        String localName = NameHelper.localizeName(vphb.getGlobalName(),vphb.getPageSpace());
        String globalName = vphb.getGlobalName();
        int baseNameI = localName.indexOf(".");
        String baseName = localName;
        if ( baseNameI > 0 ) {
            baseName = localName.substring(0,baseNameI);
        }
        globalName = NameHelper.globaliseName(baseName,vphb.getPageSpace());
        vphb.setGlobalName(globalName);
        super.successfulUpdateDispatch(request, response);
    }

    
}
