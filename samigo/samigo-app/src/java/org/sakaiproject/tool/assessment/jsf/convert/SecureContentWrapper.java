package org.sakaiproject.tool.assessment.jsf.convert;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;

@FacesConverter("org.sakaiproject.tool.assessment.jsf.convert.SecureContentWrapper")
@Slf4j
public class SecureContentWrapper implements Converter {

    private String SITE_RESOURCE_PATH = "/access/content/group/";
    private String SECURE_TOKEN_PARAMETER = "?securetoken=";

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return getAsString(arg0, arg1, arg2);
    }

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        String text = (String) arg2;
        if (StringUtils.isBlank(text)) {
            return "";
        }
        DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
        if (dbean != null) {
            String secureToken =  dbean.getSecureToken();
            String siteId = dbean.getSiteId();
            if (StringUtils.isNoneBlank(secureToken, siteId)) {
                log.debug("The secure token has been set, looking for resources for the site {} and token {}", siteId, secureToken);
                text = appendSecureTokenToContent(text, secureToken, siteId);
            }
        }
        return text;
    }

    /**
     * Parses an HTML content, extracts the URLs and appends the secure token to the ones that belong to the site and can be SECURED.
     * @param msgBody Content that could be questions or answers.
     * @param token Secure token generated during the delivery.
     * @param siteId Site identifier where the exam takes place.
     * @return String the msgBody with tokens appended to the site's URLs.
     */
    private String appendSecureTokenToContent(String msgBody, String token, String siteId){
        String replacedBody = msgBody;
        if(StringUtils.isNotEmpty(msgBody)){
            Document doc = Jsoup.parse(msgBody);

            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            List<String> references = new ArrayList<String>();
            // href ...
            for (Element link : links) {
                references.add(link.attr("abs:href"));
            }

            // img ...
            for (Element src : media) {
                references.add(src.attr("abs:src"));
            }

            for (String reference : references){
                // Only append the secure token for elements of this site.
                if (reference.contains(SITE_RESOURCE_PATH + siteId)) {
                    replacedBody = replacedBody.replace(reference, reference + SECURE_TOKEN_PARAMETER + token);
                }
            }
        }
        return replacedBody;
    }

}
