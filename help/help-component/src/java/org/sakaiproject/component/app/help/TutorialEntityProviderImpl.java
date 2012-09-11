package org.sakaiproject.component.app.help;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.help.TutorialEntityProvider;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.util.ResourceLoader;


public class TutorialEntityProviderImpl implements TutorialEntityProvider, AutoRegisterEntityProvider, RESTful{

	protected final Log log = LogFactory.getLog(getClass());
	private ResourceLoader msgs = new ResourceLoader("TutorialMessages");
	private static PropertiesConfiguration tutorialProps;
	
	private void initConfig() {
		
		URL url = getClass().getClassLoader().getResource("Tutorial.config"); 
		
		try {
			tutorialProps = new PropertiesConfiguration(); //must use blank constructor so it doesn't parse just yet (as it will split)
			tutorialProps.setReloadingStrategy(new InvariantReloadingStrategy());	//don't watch for reloads
			tutorialProps.setThrowExceptionOnMissing(false);	//throw exception if no prop
			tutorialProps.setDelimiterParsingDisabled(true); //don't split properties
			tutorialProps.load(url); //now load our file
		} catch (ConfigurationException e) {
			log.error(e.getClass() + ": " + e.getMessage());
			return;
		}
	}
	
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
		if(tutorialProps == null){
			initConfig();
			if(tutorialProps == null){
				return null;
			}
		}
		String previousUrl = tutorialProps.getString(ref.getId() + ".previousUrl");
		String nextUrl = tutorialProps.getString(ref.getId() + ".nextUrl");
		Map valuesMap = new HashMap<String, String>();
		valuesMap.put("selection", tutorialProps.getString(ref.getId() + ".selection"));
		valuesMap.put("title", msgs.get(ref.getId() + ".title"));
		valuesMap.put("dialog", tutorialProps.getString(ref.getId() + ".dialog"));
		valuesMap.put("positionTooltip", tutorialProps.getString(ref.getId() + ".positionTooltip"));
		valuesMap.put("positionTarget", tutorialProps.getString(ref.getId() + ".positionTarget"));
		valuesMap.put("fadeout", tutorialProps.getString(ref.getId() + ".fadeout"));
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
