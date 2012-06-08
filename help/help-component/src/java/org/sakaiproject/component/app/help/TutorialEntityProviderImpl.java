package org.sakaiproject.component.app.help;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.api.app.help.TutorialEntityProvider;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.util.ResourceLoader;


public class TutorialEntityProviderImpl implements TutorialEntityProvider, AutoRegisterEntityProvider, RESTful{

	private ResourceLoader msgs = new ResourceLoader("TutorialMessages");
	
	@Override
	public String getEntityPrefix() {
		return ENTTITY_PREFIX;
	}

	@Override
	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		return null;
	}

	@Override
	public Object getSampleEntity() {
		return null;
	}

	@Override
	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
	}

	@Override
	public Object getEntity(EntityReference ref) {
		String previousUrl = msgs.getString(ref.getId() + ".previousUrl");
		String nextUrl = msgs.getString(ref.getId() + ".nextUrl");
		Map valuesMap = new HashMap<String, String>();
		valuesMap.put("selection", msgs.get(ref.getId() + ".selection"));
		valuesMap.put("title", msgs.get(ref.getId() + ".title"));
		valuesMap.put("dialog", msgs.get(ref.getId() + ".dialog"));
		valuesMap.put("positionTooltip", msgs.get(ref.getId() + ".positionTooltip"));
		valuesMap.put("positionTarget", msgs.get(ref.getId() + ".positionTarget"));
		valuesMap.put("fadeout", msgs.get(ref.getId() + ".fadeout"));
		valuesMap.put("previousUrl", previousUrl);
		valuesMap.put("nextUrl", nextUrl);
		
		//build the body html:
		String body = msgs.getString(ref.getId() + ".body");
		
		//build footer html:
		String footerHtml = "<br/><br/><div style='min-width: 120px; background: #ddd;'>";
		if(previousUrl != null && !"".equals(previousUrl)){
			footerHtml += "<div style='float:left'><a href='#' class='qtipLinkButton' onclick='previousClicked=true;showTutorialPage(\"" + previousUrl + "\");'><img src='/library/image/silk/arrow_left-grey.png'>&nbsp;" + msgs.getString("previous") + "</a></div>";
		}
		if(nextUrl != null && !"".equals(nextUrl)){
			footerHtml += "<div style='float:right'><a href='#' class='qtipLinkButton' onclick='showTutorialPage(\"" + nextUrl + "\");'>" + msgs.getString("next") + "&nbsp;<img src='/library/image/silk/arrow_right-grey.png'></a></div>";
		}
		footerHtml += "</div>";
		body += footerHtml;
		
		valuesMap.put("body", body);
		
		return valuesMap;
	}

	@Override
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	}

	@Override
	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}

	@Override
	public String[] getHandledOutputFormats() {
		return null;
	}

	@Override
	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

}
