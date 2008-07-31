package org.sakaiproject.content.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.content.migration.api.RecursiveMigrator;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * This is a version of the JCR Migration that just walks through the entire
 * Resources tree recursively and copies everything.  There is not persistent
 * queue, so this would only work if you have your system turned off from all
 * users.
 * 
 * In reality, this is mostly so I can debug and analyze some threading issues
 * I keep running into with the mixture of CHS and JackRabbit both being used
 * when copying.
 * 
 * @author sgithens
 *
 */
public class RecursiveMigratorImpl implements RecursiveMigrator {
	private static final Log log = LogFactory.getLog(RecursiveMigratorImpl.class);
	
    private ContentHostingService contentHostingService;
    private JCRService jcrService;
    private ContentToJCRCopier contentToJCRCopier;
    private SessionManager sessionManager;
    private UserDirectoryService userDirectoryService;
    private AuthzGroupService authzGroupService;

    private List<String> thingsToMigrate = new ArrayList<String>();
    private int progress = 0;
    private Session jcrSession = null;
    private boolean started = false;
    
    public void runRecursiveMigration() {
    	runRecursiveMigration("/");
    }

    public void runRecursiveMigration(String startDirectory) {
        log.info("Running Recursive Migration");

        try {
			jcrSession = jcrService.getSession();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ContentCollection root = null;
		try {
			root = contentHostingService.getCollection(startDirectory);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> children = root.getMembers();
        for (String id: children) {
            try {
				doContentResource(jcrSession, id, 1);
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        // Now that we've recursed and created the list of things to migrate,
        // let's migrate them.
        Timer timer = new Timer();
        progress = 0;

        TimerTask task = new TimerTask() {
            public void run() {
                if (started == false) { 
                    becomeAdmin();
                    started = true;
                }
                
                if (progress < thingsToMigrate.size()) {
                    String migrateMe = thingsToMigrate.get(progress);
                    log.info("Going to migrate: " + migrateMe);
                    if (migrateMe.endsWith("/")) {
                        contentToJCRCopier.copyCollectionFromCHStoJCR(jcrSession, migrateMe);
                    }
                    else {
                        contentToJCRCopier.copyResourceFromCHStoJCR(jcrSession, migrateMe);
                    }
                    progress++;
                }
                else {
                    log.info("All Done Migrating from the TimerTask");
                }
            }
        };

        timer.schedule(task, 1000, 50);
    }

    private void becomeAdmin() {
        User u = null;
        try {
            u = userDirectoryService.getUserByEid("admin");
        } catch (UserNotDefinedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        org.sakaiproject.tool.api.Session s = sessionManager.getCurrentSession();

        s.setUserEid(u.getEid());
        s.setUserId(u.getId());
        s.setActive();
        sessionManager.setCurrentSession(s);
        authzGroupService.refreshUser(u.getId());
    }


    public void doContentResource(Session session, String path, int depth) throws IdUnusedException, TypeException, PermissionException {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < depth*2; i++) {
            strBuilder.append(" ");
        }

        if (path.endsWith("/")) {
            String[] parts = path.split("/");
            strBuilder.append("+ " + parts[parts.length-1]);
            //contentToJCRCopier.copyCollectionFromCHStoJCR(session, path);
            thingsToMigrate.add(path);
            ContentCollection collection = contentHostingService.getCollection(path);
            List<String> children = collection.getMembers();
            for (String id: children) {
                doContentResource(session, id, depth+1);
            }
        }
        else {
            strBuilder.append("- " + path.substring(path.lastIndexOf("/")+1));
            //contentToJCRCopier.copyResourceFromCHStoJCR(session, path);
            thingsToMigrate.add(path);
        }

        //System.out.println(strBuilder.toString());
    }

    /*
     * Boilerplate Getters/Setters Below
     */
    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setJcrService(JCRService jcrService) {
        this.jcrService = jcrService;
    }

    public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier) {
        this.contentToJCRCopier = contentToJCRCopier;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

}
