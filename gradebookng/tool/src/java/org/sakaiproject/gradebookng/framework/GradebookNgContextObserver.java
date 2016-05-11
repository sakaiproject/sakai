package org.sakaiproject.gradebookng.framework;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;

/**
 * Sakai context observer that responds to changes in the site (ie tools added/deleted etc) so that we can respond and ensure everything is
 * set.
 *
 * Note that we must also be an EntityProducer so we extend GradebookNgEntityProducer which handles that.
 */
@Slf4j
public class GradebookNgContextObserver extends GradebookNgEntityProducer implements ContextObserver {

	@Override
	public void contextCreated(final String context, final boolean toolPlacement) {
		// A new site has been created with this tool. Ensure we have a gradebook
		if (toolPlacement && !this.gradebookFrameworkService.isGradebookDefined(context)) {
			log.debug("Gradebook NG added to site " + context + ". Bootstrapping a gradebook.");
			this.gradebookFrameworkService.addGradebook(context, context);
		}

	}

	@Override
	public void contextUpdated(final String context, final boolean toolPlacement) {
		if (toolPlacement) {
			// Site has been edited and this tool has been added
			log.debug("Gradebook NG added to site " + context + ". Bootstrapping a gradebook.");
			if (!this.gradebookFrameworkService.isGradebookDefined(context)) {
				this.gradebookFrameworkService.addGradebook(context, context);
			}
		} else {
			// Site has been edited and this tool has been removed
			// Do nothing, this may have been an error and we don't want to lose the gradebook data
			log.debug("Gradebook NG removed from site " + context + " but any data will remain until site deletion.");
		}
	}

	@Override
	public void contextDeleted(final String context, final boolean toolPlacement) {
		// Site has been deleted
		if (this.gradebookFrameworkService.isGradebookDefined(context)) {
			log.debug("Site " + context + " has been deleted. Removing associated gradebook data.");
			try {
				this.gradebookFrameworkService.deleteGradebook(context);
			} catch (final GradebookNotFoundException e) {
				log.debug("Couldnt find gradebook. Nothing to delete.", e);
			}
		}
	}

}
