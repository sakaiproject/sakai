package org.sakaiproject.gradebookng.framework;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;

/**
 * Sakai context observer that responds to changes in the site (ie tools added/deleted etc) 
 * so that we can respond and ensure everything is set.
 *
 * Note that we must also be an EntityProducer so we extend GradebookNgEntityProducer which handles that.
 */
@CommonsLog
public class GradebookNgContextObserver extends GradebookNgEntityProducer implements ContextObserver {

	private static final String[] TOOL_IDS = {"sakai.gradebookng"};
	
	@Setter
	private GradebookFrameworkService gradebookFrameworkService;
	
	@Override
	public void contextCreated(String context, boolean toolPlacement) {
		// A new site has been created with this tool. Ensure we have a gradebook
		if (toolPlacement && !gradebookFrameworkService.isGradebookDefined(context)) {
			log.debug("Gradebook NG added to site " + context + ". Bootstrapping a gradebook.");
			gradebookFrameworkService.addGradebook(context, context);
		}
		
	}

	@Override
	public void contextUpdated(String context, boolean toolPlacement) {
		if (toolPlacement) {
			// Site has been edited and this tool has been added
			log.debug("Gradebook NG added to site " + context + ". Bootstrapping a gradebook.");
			if (!gradebookFrameworkService.isGradebookDefined(context)) {
				gradebookFrameworkService.addGradebook(context, context);
			}
		} else {
			// Site has been edited and this tool has been removed
			// Do nothing, this may have been an error and we don't want to lose the gradebook data
			log.debug("Gradebook NG removed from site " + context + " but any data will remain until site deletion.");
		}
	}

	@Override
	public void contextDeleted(String context, boolean toolPlacement) {
		// Site has been deleted
		if (gradebookFrameworkService.isGradebookDefined(context)) {
			log.debug("Site " + context + " has been deleted. Removing associated gradebook data.");
			try {
				gradebookFrameworkService.deleteGradebook(context);
			} catch (GradebookNotFoundException e) {
				log.debug("Couldnt find gradebook. Nothing to delete." ,e);
			}
		}
	}

	@Override
	public String[] myToolIds() {
		return TOOL_IDS;
	}

}
