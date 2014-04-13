ArchiveService2Impl allows you to customize which entities are imported during the merge
process. Previously, the list of allowable services was Hardcoded in the Service itself.
All other behaviour, as well as the API, are the same.

Configuration: 
All configuration is in archive/archive-impl/pack/src/webapp/WEB-INF/components.xml

1. Changing the path for archived files
	Change the "storagePath" property on the "org.sakaiproject.archive.api.ArchiveService" bean.

2. Changing Services whose entities are merged (impl2)
	A. You have the option of merging any and all entities from all Services
		Bean: org.sakaiproject.archive.api.ArchiveService
		Property: mergeFilterSakaiServices
		Possible Values: 
			False - All Services are merged
			True  - Services that are merged are taken from the mergeFilteredSakaiServices property
	B. To specify which services are merged in use the mergeFilteredSakaiServices property
	   This is only used is 'mergeFilterSakaiServices' is set to true.
		Bean: org.sakaiproject.archive.api.ArchiveService
		Property: mergeFilteredSakaiServices  
		Values: Takes a list of Strings. Each value should be the unqualified name of the Service.
			For example, org.sakaiproject.announcement.api.AnnouncementService, would just be
			AnnouncementService.

3. Changing Roles for which entities are merged
	Bean: org.sakaiproject.archive.api.ArchiveService
	Properties: mergeFilterSakaiRoles, mergeFilteredSakaiRoles
	These properties mimic the properties in 3, in that they control that only merges created by XYZ Roles
	are merged. Note: Some testing needs to be done to make sure this is really observed in all Services. 

TODO: describe the import architecture

UPDATE 13-APR-2014: The original ArchiveService impl has been removed. See SAK-25866.