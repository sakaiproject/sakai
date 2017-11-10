/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitemanage.impl.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserPermissionException;

import com.github.javafaker.Faker;


/**
 * <p>
 * This job will create a default number of sites, students, instructors. It will then enroll
 * all instructors to each site and randomly enroll students to each site.
 * </p>
 * <p>
 * Finally it will randomly create text documents and add them randomly to the newly created 
 * sites.
 * </p> 
 * 
 * @author Earle Nietzel (enietzel@anisakai.com)
 * @author John Bush (jbush@anisakai.com)
 *
 */
@Slf4j
public class SeedSitesAndUsersJob implements Job {
	
	@Setter
	private ServerConfigurationService serverConfigurationService;
	@Setter
	private UserDirectoryService userDirectoryService;
	@Setter
	private SiteService siteService;
	@Setter
	private SecurityAdvisor securityAdvisor;
	@Setter
	private SecurityService securityService;
	@Setter
	private SessionManager sessionManager;
	@Setter
	private ContentHostingService contentHostingService;
	@Setter
	private SqlService sqlService;

	private int numberOfSites = 5;
	private int numberOfStudents = 100;
	private int numberOfEnnrollmentsPerSite = 50;
	private int numberOfInstructorsPerSite = 1;
	private String emailDomain = "mailinator.com";

	private long repositorySize = 10485760;        //  10 MB
	//private long repositorySize = 1073741824L;   //   1 GB
	//private long repositorySize = 10737418240L;  //  10 GB
	//private long repositorySize = 21474836480L;  //  20 GB
	//private long repositorySize = 42949672960L;  //  40 GB
	//private long repositorySize = 64424509440L;  //  60 GB
	//private long repositorySize = 107374182400L; // 100 GB


	private Faker faker = new Faker();
	private Random randomGenerator = new Random();

	private Map<String, User> students;
	private Map<String, User> instructors;
	private Map<String, Site> sites;

	public void init() {
		numberOfSites = serverConfigurationService.getInt("site.seed.create.sites", 5);
		numberOfStudents = serverConfigurationService.getInt("site.seed.create.students", 100);
		numberOfEnnrollmentsPerSite = serverConfigurationService.getInt("site.seed.enrollments.per.site", 50);
		numberOfInstructorsPerSite = serverConfigurationService.getInt("site.seed.instructors.per.site", 1);
		emailDomain = serverConfigurationService.getString("site.seed.email.domain", "mailinator.com");

		try {
	        repositorySize = Long.parseLong(serverConfigurationService.getString("site.seed.repository.size", "10485760"));
        } catch (NumberFormatException nfe) {
        	repositorySize = 1073741824L;
        }
			
		// Create our security advisor.
		securityAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
	}

	private void seedData() {		
		long totalBytes = 0;
		
		while (totalBytes < repositorySize) {
			log.info("current repository size: {}", totalBytes);
			
			Site site = getRandomSite();
			String collectionName = getCollectionName(site);
			ContentCollection collection = null;
			try {
				collection = contentHostingService.getCollection(collectionName);
			} catch (IdUnusedException e) {
				collection = createCollection(collectionName);
				contentHostingService.commitCollection((ContentCollectionEdit) collection);
			} catch (TypeException te) {
				log.error("wrong collection type: ", te);
            } catch (PermissionException pe) {
    			log.error("collection permission: ", pe);
            }
			
			if (collection != null) {

				// 5k paragraphs works out to on average docs around 1/2 MB in size byte[] rawFile =
				// StringUtils.join(faker.paragraphs(randomGenerator.nextInt(5000)), "\n\n").getBytes();
				byte[] rawFile = StringUtils.join(faker.lorem().paragraphs(randomGenerator.nextInt(500)), "\n\n").getBytes();
				String fileName = StringUtils.join(faker.lorem().words(4), "-") + "_" + String.valueOf(randomGenerator.nextInt(5000));
				
				try {
					ContentResourceEdit resourceEdit = contentHostingService.addResource(collectionName + fileName + ".txt");
					ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
					props.addProperty(ResourceProperties.PROP_DESCRIPTION, "created for testing");
					props.addProperty(ResourceProperties.PROP_PUBVIEW, "false");
					resourceEdit.setContent(rawFile);
					resourceEdit.setContentType("text/plain");
					contentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);
				} catch (Exception e) {
					log.error("cannot add resource: {}{}.txt", collectionName, fileName, e);
				}
				totalBytes += rawFile.length;
			} else {
				log.error("could not get collection: {}", collectionName);
				break;
			}
		}
	}
	
	private long getSizeOfResources() {
		StringBuffer sb = new StringBuffer();
		for (Site site : sites.values()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
	        sb.append("'").append(site.getId()).append("'");
        }

		return new SizeOfResourcesQuery(sb.toString()).run().returnResult();
	}

	private Site getRandomSite() {
		String[] siteIds = sites.keySet().toArray(new String[] {});
		String siteId = siteIds[randomGenerator.nextInt(siteIds.length - 1)];
		return sites.get(siteId);
	}

	private ContentCollectionEdit createCollection(String collectionName) {
		ContentCollectionEdit collectionEdit = null;
		try {
			collectionEdit = contentHostingService.addCollection(collectionName);
			collectionEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "searchdata");
		} catch (IdUsedException iue) {
			log.error("existing collection: ", iue);
		} catch (IdInvalidException iie) {
			log.error("invalid collection id: ", iie);
		} catch (PermissionException pe) {
			log.error("collection permission: ", pe);
		} catch (InconsistentException ie) {
			log.error("collection inconsistent: ", ie);
		}
		return collectionEdit;
	}

	private String getCollectionName(Site site) {
		return "/group/" + site.getId() + "/searchdata/";
	}

	private void createSites() throws PermissionException, IdInvalidException, IdUsedException, IdUnusedException {
		for (long i = 0; i < numberOfSites; i++) {
			try {
	            createSite(faker.bothify("????_###?_####").toUpperCase(), "course");
            } catch (IdUsedException e) {
            	createSite(faker.bothify("????_###?_####").toUpperCase(), "course");
            }
		}
	}
	
	private void createSite(String title, String type) throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException {
		Site site = siteService.addSite(title, type);
		site.setPublished(true);
		site.setTitle(title);
		site.addPage().addTool("sakai.search");
		site.addPage().addTool("sakai.resources");
		site.addPage().addTool("sakai.siteinfo");
		siteService.save(site);
		sites.put(site.getId(), site);
		log.info("created site: {}", site.getId());
	}

	private void createStudents() {
		for (long i = 1; i <= numberOfStudents; i++) {
			User user = createUser("registered");
			if (user != null) {
				students.put(user.getEid(), user);
			}
			if (i % 100 == 0) {
				log.info("created {} random student accounts", i);
			}
		}
	}

	private void createInstructors() {
		for (long i = 0; i < numberOfInstructorsPerSite; i++) {
			User user = createUser("maintain");
			if (user != null) {
				instructors.put(user.getEid(), user);
			}
		}
	}
	
	private User createUser(String userType) {
		User user = null;
			String lastName = faker.name().lastName();
			String eid = faker.numerify("#########");
			try {
	            user = userDirectoryService.addUser(null, eid, faker.name().firstName(), lastName, eid + "@" + emailDomain, faker.letterify("???????"), userType, null);
            } catch (UserIdInvalidException uiue) {
            	log.error("invalid userId: ", uiue);
            } catch (UserAlreadyDefinedException uade) {
            	log.error("already exists: ", uade);
            	user = createUser(userType);
            } catch (UserPermissionException upe) {
            	log.error("permission: ", upe);
            }
			
		return user;
	}

	private void createEnrollments() {
		for (String siteId : sites.keySet()) {
			Site site = sites.get(siteId);
			Set<String> enrollments = getRandomUsers(students.keySet());
			for (String enrollment : enrollments) {
				User user = students.get(enrollment);
				site.addMember(user.getId(), "Student", true, false);
			}
			for (User instructor : instructors.values()) {
				site.addMember(instructor.getId(), "Instructor", true, false);
            }
			try {
				siteService.save(site);
			} catch (IdUnusedException iue) {
				log.error("site doesn't exist:", iue);
			} catch (PermissionException pe) {
				log.error("site save permission:", pe);
			}
		}
	}

	private Set<String> getRandomUsers(Set<String> pool) {
		if (pool.size() <= numberOfEnnrollmentsPerSite) {
			return pool;
		}

		List<String> randomizedPool = new ArrayList<>(pool);
		Collections.shuffle(randomizedPool, randomGenerator);

		return new HashSet<>(randomizedPool.subList(0, numberOfEnnrollmentsPerSite));
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("SeedSitesAndUsersJob started.");
		
		students = new HashMap<String, User>();
		instructors = new HashMap<String, User>();
		sites = new HashMap<String, Site>();

		Session session = sessionManager.getCurrentSession();
		String currentUser = session.getUserId();
		
		session.setUserId("admin");
		session.setUserEid("admin");
		
		securityService.pushAdvisor(securityAdvisor);

		try {
			createSites();
			
			createInstructors();
			createStudents();
			createEnrollments();

			seedData();
		} catch (Exception e) {
			log.error("executing job: ", e);
		}
		
		securityService.popAdvisor(securityAdvisor);
		
		session.setUserId(currentUser);
		session.setUserEid(currentUser);
		
		students = null;
		instructors = null;
		sites = null;
		
		log.info("SeedSitesAndUsersJob completed.");
	}

	abstract class Query<T> {
		Connection connection;
		PreparedStatement statement;
		ResultSet result;
		String sql;

		public Query(String sql) {
			this.sql = sql;
		}

		Query<T> run() {
			try {
				connection = sqlService.borrowConnection();
				statement = connection.prepareStatement(sql);
				result = statement.executeQuery();

				processResult();
			} catch (Exception e) {
				log.error("Query.run, running query: ", e);
			} finally {
				if (result != null) {
					try {
						result.close();
					} catch (SQLException e) {
						log.error("Query.run, releasing result: ", e);
					}
				}
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						log.error("Query.run, closing statement: ", e);
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						log.error("Query.run, closing connection: ", e);
					}
				}
			}
			return this;
		}

		abstract void processResult() throws SQLException;

		abstract T returnResult();
	}

	class RandomSiteQuery extends Query<Site> {
		Site site;

		public RandomSiteQuery(String sql) {
			super("SELECT SITE_ID FROM sakai_site where TYPE = '" + sql + "' ORDER BY RAND() LIMIT 1");
		}

		@Override
		public void processResult() throws SQLException {
			while (result.next()) {
				String siteId = result.getString(1);
				try {
					site = siteService.getSite(siteId);
				} catch (IdUnusedException e) {
					log.warn("RandomSiteQuery.processResult, No sites found", e);
				}
			}
		}

		@Override
		Site returnResult() {
			return site;
		}
	}

	class SizeOfResourcesQuery extends Query<Long> {
		Long size;

		public SizeOfResourcesQuery(String sql) {
			super("SELECT SUM(cr.FILE_SIZE) FROM content_resource cr JOIN sakai_site ss on (cr.CONTEXT = ss.SITE_ID) where ss.SITE_ID IN (" + sql + ")");
		}

		@Override
		void processResult() throws SQLException {
			if (result.next()) {
				size = result.getLong(1);
			}
		}

		@Override
		Long returnResult() {
			return size;
		}
	}

	class CountSitesQuery extends Query<Long> {
		Long size;

		public CountSitesQuery(String sql) {
			super("SELECT SUM(cr.FILE_SIZE) FROM content_resource cr JOIN sakai_site ss on (cr.CONTEXT = ss.SITE_ID) where ss.TYPE IN (" + sql + ")");
		}

		@Override
		void processResult() throws SQLException {
			if (result.next()) {
				size = result.getLong(1);
			}
		}

		@Override
		Long returnResult() {
			return size;
		}
	}
	
	class CountUsersQuery extends Query<Long> {
		Long count;

		public CountUsersQuery(String sql) {
			super("select COUNT(USER_ID) FROM sakai_user WHERE TYPE IN (" + sql + ")");
		}

		@Override
		void processResult() throws SQLException {
			if (result.next()) {
				count = result.getLong(1);
			}
		}

		@Override
		Long returnResult() {
			return count;
		}
	}

}
