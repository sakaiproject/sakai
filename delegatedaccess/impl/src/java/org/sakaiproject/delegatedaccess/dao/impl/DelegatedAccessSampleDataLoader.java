package org.sakaiproject.delegatedaccess.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.delegatedaccess.jobs.DelegatedAccessSiteHierarchyJob;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;

public class DelegatedAccessSampleDataLoader {
	private SiteService siteService;
	private DelegatedAccessSiteHierarchyJob delegatedAccessSiteHierarchyJob;
	private SecurityService securityService;

	private List<String> schools = Arrays.asList("MUSIC", "MEDICINE", "EDUCATION");
	private List<String> depts = Arrays.asList("DEPT1", "DEPT2", "DEPT3");
	private List<String> subjs = Arrays.asList("SUBJ1", "SUBJ2","SUBJ3");
	
	public void init(){
		if(siteService == null || securityService == null || delegatedAccessSiteHierarchyJob == null){
			return;
		}
		// Cheating to become admin in order to add sites
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
		try{
			securityService.pushAdvisor(yesMan);
			for(String school : schools){
				for(String dept : depts){
					for(String subject : subjs){
						for(int courseNum = 101; courseNum < 600; courseNum += 25){
							String siteid = "DAC-" + school + "-" + dept + "-" + subject + "-" + courseNum; 
							String title = siteid;
							String description = siteid;
							String shortdesc = siteid;

							Site siteEdit = null;
							try {
								siteEdit = siteService.addSite(siteid, "course");
								siteEdit.setTitle(title);
								siteEdit.setDescription(description);
								siteEdit.setShortDescription(shortdesc);
								siteEdit.setPublished(true);
								siteEdit.setType("course");
								
								//for some reason the course template may not work
								if(siteEdit.getTool("sakai.siteinfo") == null){
									SitePage page = siteEdit.addPage();
									page.setTitle("Site Info");
									page.addTool("sakai.siteinfo");
								}
								if(siteEdit.getRole("Instructor") == null){
									siteEdit.addRole("Instructor");
								}
								
								ResourcePropertiesEdit propEdit = siteEdit.getPropertiesEdit();
								propEdit.addProperty("School", school);
								propEdit.addProperty("Department", dept);
								propEdit.addProperty("Subject", subject);
								siteService.save(siteEdit);

							} catch (IdInvalidException e) {
								e.printStackTrace();
							} catch (IdUsedException e) {
								e.printStackTrace();
								//this means that we have already ran this, lets quit
								return;
							} catch (PermissionException e) {
								e.printStackTrace();
							} catch (IdUnusedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			//now that the sites have been added, lets run the hierarhcy job
			try {
				delegatedAccessSiteHierarchyJob.execute(null);
			} catch (JobExecutionException e) {
				e.printStackTrace();
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			securityService.popAdvisor(yesMan);
		}
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public DelegatedAccessSiteHierarchyJob getDelegatedAccessSiteHierarchyJob() {
		return delegatedAccessSiteHierarchyJob;
	}

	public void setDelegatedAccessSiteHierarchyJob(
			DelegatedAccessSiteHierarchyJob delegatedAccessSiteHierarchyJob) {
		this.delegatedAccessSiteHierarchyJob = delegatedAccessSiteHierarchyJob;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
