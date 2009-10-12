USING SAKAI SERVICES OUTSIDE THE WEB SERVER

The ComponentContainerEmulator utility class makes it possible to run real
deployed Sakai component services without going through a web server.
It looks for a Tomcat deployment, puts shared JAR libraries on the classpath,
points to a Sakai "components" directory and a Sakai home directory, and
then starts the Sakai component manager, at which point your own code
can call Sakai services more or less as if it was running inside a Sakai
component.

Josh Holtzman originally implemented this functionality to support automated
testing with service-level integration. However, it also provides an easy way to
implement complex data migrations, to load large amounts of test data, or to
do anything else that requires access to Sakai services but doesn't need to
run in a web application or a Quartz job.

As an example, the appended code will finalize grades in all gradebooks in course
sites associated with the term "Spring 2008":

/******************************/

import static org.sakaiproject.test.ComponentContainerEmulator.getService;
import static org.sakaiproject.test.ComponentContainerEmulator.startComponentManager;
import static org.sakaiproject.test.ComponentContainerEmulator.stopComponentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class GradesFinalizerExec {
	public static String TERM_EID = "Spring 2008";
	public static void main(String[] args) {
		startComponentManager();
		SiteService siteService = getService(SiteService.class);
		GradebookService gradebookService = getService(GradebookService.class);
		actAsUserEid("admin");

		Map<String, String> sitePropertyCriteria = new HashMap<String, String>();
		sitePropertyCriteria.put("term_eid", TERM_EID);
		List<Site> sites = siteService.getSites(SiteService.SelectionType.NON_USER, null, null, sitePropertyCriteria, SiteService.SortType.NONE, null);
		for (Site site : sites) {
			String gradebookUid = site.getId();
			if (gradebookService.isGradebookDefined(gradebookUid)) {
				gradebookService.finalizeGrades(gradebookUid);
			}
		}
		stopComponentManager();
	}

	/**
	 * TODO This piece of logic is repeated in enough places that we shoud make
	 * it a central utility method.
	 */
	public void actAsUserEid(String userEid) {
		UserDirectoryService userDirectoryService = getService(UserDirectoryService.class);
		SessionManager sessionManager = getService(SessionManager.class);
		String userId;
		try {
			userId = userDirectoryService.getUserId(userEid);
		} catch (UserNotDefinedException e) {
			log.error("Could not act as user EID=" + userEid, e);
			return;
		}
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userEid);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
	}
}
