package org.sakaiproject.tool.messageforums.jsf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.faces.component.UIComponent;

import com.sun.faces.renderkit.html_basic.OutputLinkRenderer;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is a wrapper for {@link OutputLinkRenderer} to escape ampersands in URL parameters.
 * This is required until Sakai uses JSF 1.2_11 or above versions.
 */
@Slf4j
public class EscapedOutputLinkRenderer extends OutputLinkRenderer {

	@Override
	protected Param[] getParamList(final UIComponent command) {
		Param[] paramList = super.getParamList(command);
		for (int i = 0, len = paramList.length; i < len; i++) {
			String pn = paramList[i].name;
			if (pn != null && pn.length() != 0) {
				String pv = paramList[i].value;
				try {
					pn = URLEncoder.encode(pn.replaceAll("&lt;", "<").replaceAll("&gt;", ">"), "UTF-8").replaceAll("\\+", "%20");
					if (pv != null && pv.length() != 0) {
						pv = URLEncoder.encode(pv.replaceAll("&lt;", "<").replaceAll("&gt;", ">"), "UTF-8").replaceAll("\\+", "%20");
					}
					paramList[i].name=pn;
					paramList[i].value=pv;
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return paramList;
	}
}
